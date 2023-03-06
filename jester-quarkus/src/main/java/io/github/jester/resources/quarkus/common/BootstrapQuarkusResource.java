package io.github.jester.resources.quarkus.common;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.condition.OS;

import io.github.jester.api.Dependency;
import io.github.jester.core.ServiceContext;
import io.github.jester.utils.ClassPathUtils;
import io.github.jester.utils.FileUtils;
import io.github.jester.utils.MapUtils;
import io.github.jester.utils.PathTestHelper;
import io.github.jester.utils.PropertiesUtils;
import io.github.jester.utils.QuarkusUtils;
import io.github.jester.utils.ReflectionUtils;
import io.quarkus.bootstrap.app.AugmentAction;
import io.quarkus.bootstrap.app.AugmentResult;
import io.quarkus.bootstrap.app.CuratedApplication;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.bootstrap.model.AppDependency;

public class BootstrapQuarkusResource extends QuarkusResource {

    private static final String NATIVE_RUNNER = "-runner";
    private static final String EXE = ".exe";
    private static final String JVM_RUNNER = "-runner.jar";
    private static final String QUARKUS_APP = "quarkus-app";
    private static final String QUARKUS_RUN = "quarkus-run.jar";
    private static final String DEPENDENCY_SCOPE_DEFAULT = "compile";
    private static final String QUARKUS_GROUP_ID_DEFAULT = "io.quarkus";
    private static final int DEPENDENCY_DIRECT_FLAG = 0b000010;

    private final Path location;
    private final Path runner;
    private final String quarkusVersion;

    private Class<?>[] appClasses;
    private List<AppDependency> forcedDependencies = Collections.emptyList();
    private boolean requiresCustomBuild;
    private Map<String, String> propertiesSnapshot;

    public BootstrapQuarkusResource(ServiceContext context, String location, Class<?>[] classes,
            Dependency[] forcedDependencies, boolean forceBuild, String quarkusVersion) {
        super(context);

        this.location = Path.of(location);
        this.quarkusVersion = quarkusVersion;
        if (!Files.exists(this.location)) {
            throw new RuntimeException("Quarkus location does not exist.");
        }

        // init source classes
        requiresCustomBuild = true;
        appClasses = classes;
        if (appClasses == null || appClasses.length == 0) {
            appClasses = ClassPathUtils.findAllClassesFromSource(this.location);
            requiresCustomBuild = false;
        }

        // init dependencies
        if (forcedDependencies != null && forcedDependencies.length > 0) {
            requiresCustomBuild = true;
            this.forcedDependencies = Stream.of(forcedDependencies).map(d -> {
                String groupId = defaultIfEmpty(PropertiesUtils.resolveProperty(d.groupId()), QUARKUS_GROUP_ID_DEFAULT);
                String version = defaultIfEmpty(PropertiesUtils.resolveProperty(d.version()),
                        defaultIfEmpty(quarkusVersion, QuarkusUtils.getVersion()));
                AppArtifact artifact = new AppArtifact(groupId, d.artifactId(), version);
                // Quarkus introduces a breaking change in 2.3:
                // https://github.com/quarkusio/quarkus/commit/0c85b27c4046c894c181ffea367fca503d1c682c
                if (QuarkusUtils.isQuarkusVersion2Dot3OrAbove()) {
                    return ReflectionUtils.createInstance(AppDependency.class, artifact, DEPENDENCY_SCOPE_DEFAULT,
                            new int[] { DEPENDENCY_DIRECT_FLAG });
                }

                return new AppDependency(artifact, DEPENDENCY_SCOPE_DEFAULT);
            }).collect(Collectors.toList());
        }

        requiresCustomBuild = requiresCustomBuild || forceBuild || isNotEmpty(quarkusVersion);

        runner = tryToReuseOrBuildRunner();
    }

    public Path getRunner() {
        return runner;
    }

    private Path tryToReuseOrBuildRunner() {
        Optional<String> runnerLocation = Optional.empty();
        if (!containsBuildProperties() && !requiresCustomBuild) {
            Path targetLocation = location.resolve(PropertiesUtils.TARGET);
            if (QuarkusUtils.isNativePackageType(context.getOwner())) {
                String nativeRunnerExpectedLocation = NATIVE_RUNNER;
                if (OS.WINDOWS.isCurrentOs()) {
                    nativeRunnerExpectedLocation += EXE;
                }

                runnerLocation = FileUtils.findFile(targetLocation, nativeRunnerExpectedLocation);

            } else {
                runnerLocation = FileUtils.findFile(targetLocation, JVM_RUNNER)
                        .or(() -> FileUtils.findFile(targetLocation.resolve(QUARKUS_APP), QUARKUS_RUN));
            }
        }

        if (runnerLocation.isEmpty()) {
            return buildRunner();
        } else {
            return Path.of(runnerLocation.get());
        }
    }

    private void copyResourcesToAppFolder() {
        copyResourcesInFolderToAppFolder(location.resolve(QuarkusUtils.RESOURCES_FOLDER));
        copyResourcesInFolderToAppFolder(QuarkusUtils.TEST_RESOURCES_FOLDER);
        createComputedApplicationProperties();
    }

    private void createComputedApplicationProperties() {
        Path generatedApplicationProperties = context.getServiceFolder().resolve(QuarkusUtils.APPLICATION_PROPERTIES);
        Map<String, String> map = new HashMap<>();
        // Add the content of the source application properties into the auto-generated application.properties
        if (Files.exists(generatedApplicationProperties)) {
            map.putAll(PropertiesUtils.toMap(generatedApplicationProperties));
        }
        // Then add the service properties
        map.putAll(context.getOwner().getProperties());
        // Then overwrite the application properties with the generated application.properties
        PropertiesUtils.fromMap(map, generatedApplicationProperties);
    }

    private boolean containsBuildProperties() {
        Map<String, String> differenceProperties = MapUtils.difference(context.getOwner().getProperties(),
                propertiesSnapshot);
        Set<String> properties = differenceProperties.keySet();
        if (properties.isEmpty()) {
            return false;
        }

        return properties.stream().anyMatch(QuarkusUtils::isBuildProperty);
    }

    private Path buildRunner() {
        if (appClasses.length == 0) {
            throw new RuntimeException("No classes were found at " + location
                    + ". You need to build the application before running the tests.");
        }

        if (!QuarkusUtils.isBootstrapDependencyAdded()) {
            throw new RuntimeException(
                    "To use custom classes or dependencies, you need to add the dependency `io.quarkus:quarkus-test-common`. "
                            + "Otherwise, you need to build the current module before executing the integration tests");
        }

        try {
            copyResourcesToAppFolder();
            createSnapshotOfBuildProperties();
            Path appFolder = context.getServiceFolder();

            JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class).addClasses(appClasses);
            javaArchive.as(ExplodedExporter.class).exportExplodedInto(appFolder.toFile());

            Path testLocation = PathTestHelper.getTestClassesLocation(context.getTestContext().getRequiredTestClass());

            QuarkusBootstrap.Builder builder = QuarkusBootstrap.builder().setApplicationRoot(appFolder)
                    .setMode(QuarkusBootstrap.Mode.PROD).addExcludedPath(testLocation).setIsolateDeployment(true)
                    .setProjectRoot(testLocation).setBaseName(context.getName()).setTargetDirectory(appFolder);

            if (isNotEmpty(quarkusVersion)) {
                Properties properties = new Properties();
                properties.put("quarkus.application.version", quarkusVersion);
                builder.setBuildSystemProperties(properties);
            }

            if (!forcedDependencies.isEmpty()) {
                // The method setForcedDependencies signature changed from `List<AppDependency>` to `List<Dependency>`
                // where
                // Dependency is an interface of AppDependency, so it should be fine. However, the compiler fails to
                // cast it,
                // so we need to use reflection to sort it out for the most recent version and older versions.
                ReflectionUtils.invokeMethod(builder, "setForcedDependencies", forcedDependencies);
            }

            // The method `setLocalProjectDiscover y` signature changed from `Boolean` to `boolean` and this might make
            // to fail the tests at runtime when using different versions.
            // In order to workaround this, we need to invoke this method at runtime to let JVM unbox the arguments
            // properly.
            // Note that this is happening because we want to support both 2.x and 1.13.x Quarkus versions.
            // Another strategy could be to have our own version of Quarkus bootstrap.
            ReflectionUtils.invokeMethod(builder, "setLocalProjectDiscovery", true);

            configureAdditionalBuildSteps(builder);

            AugmentResult result;
            try (CuratedApplication curatedApplication = builder.build().bootstrap()) {
                Map<String, Object> buildContext = new HashMap<>();
                AugmentAction action = curatedApplication
                        .createAugmentor(JesterBuildChainCustomizerProducer.class.getName(), buildContext);
                result = action.createProductionApplication();
            }

            return Optional.ofNullable(result.getNativeResult()).orElseGet(() -> result.getJar().getPath());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to build Quarkus artifacts.", ex);
        }
    }

    private void configureAdditionalBuildSteps(QuarkusBootstrap.Builder builder) throws IOException {
        // we need to make sure all the classes needed to support the customizer flow are available at bootstrap time
        // for that purpose we add them to a new archive that is then added to Quarkus bootstrap
        Path additionalDeploymentDir = Files
                .createDirectories(context.getServiceFolder().resolve("additional-deployment"));
        JavaArchive additionalDeploymentArchive = ShrinkWrap.create(JavaArchive.class).addClasses(
                JesterBuildChainCustomizerProducer.class, JesterBuildChainCustomizerConsumer.class,
                KubernetesCustomProjectBuildStep.class);
        additionalDeploymentArchive.as(ExplodedExporter.class).exportExplodedInto(additionalDeploymentDir.toFile());
        builder.addAdditionalDeploymentArchive(additionalDeploymentDir);
    }

    private void copyResourcesInFolderToAppFolder(Path folder) {
        try (Stream<Path> binariesFound = Files.find(folder, Integer.MAX_VALUE,
                (path, basicFileAttributes) -> !Files.isDirectory(path))) {
            binariesFound.forEach(path -> {
                File fileToCopy = path.toFile();

                Path source = folder.relativize(path).getParent();
                Path target = context.getServiceFolder();
                if (source != null) {
                    // Resource is in a sub-folder:
                    target = target.resolve(source);
                    // Create subdirectories if necessary
                    target.toFile().mkdirs();
                }

                FileUtils.copyFileTo(fileToCopy, target);
            });
        } catch (IOException ex) {
            // ignored
        }
    }

    private void createSnapshotOfBuildProperties() {
        propertiesSnapshot = new HashMap<>(context.getOwner().getProperties());
    }
}
