package io.jcloud.resources.quarkus.common;

import static io.jcloud.utils.FileUtils.findFile;
import static io.jcloud.utils.PropertiesUtils.TARGET;
import static io.jcloud.utils.PropertiesUtils.resolveProperty;
import static io.jcloud.utils.QuarkusUtils.APPLICATION_PROPERTIES;
import static io.jcloud.utils.QuarkusUtils.RESOURCES_FOLDER;
import static io.jcloud.utils.QuarkusUtils.TEST_RESOURCES_FOLDER;
import static io.jcloud.utils.QuarkusUtils.isQuarkusVersion2Dot3OrAbove;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.condition.OS;

import io.jcloud.api.Dependency;
import io.jcloud.core.ServiceContext;
import io.jcloud.utils.ClassPathUtils;
import io.jcloud.utils.FileUtils;
import io.jcloud.utils.MapUtils;
import io.jcloud.utils.PropertiesUtils;
import io.jcloud.utils.QuarkusUtils;
import io.jcloud.utils.ReflectionUtils;
import io.quarkus.bootstrap.app.AugmentAction;
import io.quarkus.bootstrap.app.AugmentResult;
import io.quarkus.bootstrap.app.CuratedApplication;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.bootstrap.model.AppDependency;
import io.quarkus.test.common.PathTestHelper;

public class BootstrapQuarkusResource extends QuarkusResource {

    private static final String NATIVE_RUNNER = "-runner";
    private static final String EXE = ".exe";
    private static final String JVM_RUNNER = "-runner.jar";
    private static final String QUARKUS_APP = "quarkus-app";
    private static final String QUARKUS_RUN = "quarkus-run.jar";
    private static final String DEPENDENCY_SCOPE_DEFAULT = "compile";
    private static final String QUARKUS_GROUP_ID_DEFAULT = "io.quarkus";
    private static final int DEPENDENCY_DIRECT_FLAG = 0b000010;

    private final Path runner;

    private Class<?>[] appClasses;
    private List<AppDependency> forcedDependencies = Collections.emptyList();
    private boolean requiresCustomBuild;
    private Map<String, String> propertiesSnapshot;

    public BootstrapQuarkusResource(ServiceContext context, Class<?>[] classes, Dependency[] forcedDependencies) {
        super(context);
        // init source classes
        requiresCustomBuild = true;
        appClasses = classes;
        if (appClasses == null || appClasses.length == 0) {
            appClasses = ClassPathUtils.findAllClassesFromSource();
            requiresCustomBuild = false;
        }

        // init dependencies
        if (forcedDependencies != null && forcedDependencies.length > 0) {
            requiresCustomBuild = true;
            this.forcedDependencies = Stream.of(forcedDependencies).map(d -> {
                String groupId = StringUtils.defaultIfEmpty(resolveProperty(d.groupId()), QUARKUS_GROUP_ID_DEFAULT);
                String version = StringUtils.defaultIfEmpty(resolveProperty(d.version()), QuarkusUtils.getVersion());
                AppArtifact artifact = new AppArtifact(groupId, d.artifactId(), version);
                // Quarkus introduces a breaking change in 2.3:
                // https://github.com/quarkusio/quarkus/commit/0c85b27c4046c894c181ffea367fca503d1c682c
                if (isQuarkusVersion2Dot3OrAbove()) {
                    return ReflectionUtils.createInstance(AppDependency.class, artifact, DEPENDENCY_SCOPE_DEFAULT,
                            new int[] { DEPENDENCY_DIRECT_FLAG });
                }

                return new AppDependency(artifact, DEPENDENCY_SCOPE_DEFAULT);
            }).collect(Collectors.toList());
        }

        runner = tryToReuseOrBuildRunner();
    }

    public Path getRunner() {
        return runner;
    }

    private Path tryToReuseOrBuildRunner() {
        Optional<String> runnerLocation = Optional.empty();
        if (!containsBuildProperties() && !requiresCustomBuild) {
            if (QuarkusUtils.isNativePackageType(context.getOwner())) {
                String nativeRunnerExpectedLocation = NATIVE_RUNNER;
                if (OS.WINDOWS.isCurrentOs()) {
                    nativeRunnerExpectedLocation += EXE;
                }

                runnerLocation = findFile(TARGET, nativeRunnerExpectedLocation);

            } else {
                runnerLocation = findFile(TARGET, JVM_RUNNER)
                        .or(() -> findFile(TARGET.resolve(QUARKUS_APP), QUARKUS_RUN));
            }
        }

        if (runnerLocation.isEmpty()) {
            return buildRunner();
        } else {
            return Path.of(runnerLocation.get());
        }
    }

    private void copyResourcesToAppFolder() {
        copyResourcesInFolderToAppFolder(RESOURCES_FOLDER);
        copyResourcesInFolderToAppFolder(TEST_RESOURCES_FOLDER);
        createComputedApplicationProperties();
    }

    private void createComputedApplicationProperties() {
        Path generatedApplicationProperties = context.getServiceFolder().resolve(APPLICATION_PROPERTIES);
        Map<String, String> map = new HashMap<>();
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

            if (!forcedDependencies.isEmpty()) {
                // The method setForcedDependencies signature changed from `List<AppDependency>` to `List<Dependency>`
                // where
                // Dependency is an interface of AppDependency, so it should be fine. However, the compiler fails to
                // cast it,
                // so we need to use reflection to sort it out for the most recent version and older versions.
                ReflectionUtils.invokeMethod(builder, "setForcedDependencies", forcedDependencies);
            }

            // The method `setLocalProjectDiscovery` signature changed from `Boolean` to `boolean` and this might make
            // to fail the tests at runtime when using different versions.
            // In order to workaround this, we need to invoke this method at runtime to let JVM unbox the arguments
            // properly.
            // Note that this is happening because we want to support both 2.x and 1.13.x Quarkus versions.
            // Another strategy could be to have our own version of Quarkus bootstrap.
            ReflectionUtils.invokeMethod(builder, "setLocalProjectDiscovery", true);

            AugmentResult result;
            try (CuratedApplication curatedApplication = builder.build().bootstrap()) {
                AugmentAction action = curatedApplication.createAugmentor();
                result = action.createProductionApplication();
            }

            return Optional.ofNullable(result.getNativeResult()).orElseGet(() -> result.getJar().getPath());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to build Quarkus artifacts.", ex);
        }
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

    private Map<String, String> createSnapshotOfBuildProperties() {
        propertiesSnapshot = new HashMap<>(context.getOwner().getProperties());
        return new HashMap<>(context.getOwner().getProperties());
    }
}
