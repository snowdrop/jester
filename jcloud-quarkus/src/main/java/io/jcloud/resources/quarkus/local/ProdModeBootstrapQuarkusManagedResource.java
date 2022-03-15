package io.jcloud.resources.quarkus.local;

import static io.jcloud.utils.FileUtils.findFile;
import static io.jcloud.utils.PropertiesUtils.RESOURCE_PREFIX;
import static io.jcloud.utils.PropertiesUtils.SECRET_PREFIX;
import static io.jcloud.utils.PropertiesUtils.TARGET;
import static io.jcloud.utils.PropertiesUtils.resolveProperty;
import static io.jcloud.utils.QuarkusUtils.QUARKUS_HTTP_PORT_PROPERTY;
import static io.jcloud.utils.QuarkusUtils.isQuarkusVersion2Dot3OrAbove;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
import io.jcloud.logging.FileServiceLoggingHandler;
import io.jcloud.logging.Log;
import io.jcloud.logging.LoggingHandler;
import io.jcloud.resources.quarkus.QuarkusManagedResource;
import io.jcloud.utils.ClassPathUtils;
import io.jcloud.utils.FileUtils;
import io.jcloud.utils.MapUtils;
import io.jcloud.utils.ProcessBuilderProvider;
import io.jcloud.utils.ProcessUtils;
import io.jcloud.utils.PropertiesUtils;
import io.jcloud.utils.QuarkusUtils;
import io.jcloud.utils.ReflectionUtils;
import io.jcloud.utils.SocketUtils;
import io.quarkus.bootstrap.app.AugmentAction;
import io.quarkus.bootstrap.app.AugmentResult;
import io.quarkus.bootstrap.app.CuratedApplication;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.bootstrap.model.AppDependency;
import io.quarkus.builder.Version;
import io.quarkus.test.common.PathTestHelper;

public class ProdModeBootstrapQuarkusManagedResource extends QuarkusManagedResource {
    public static final String APPLICATION_PROPERTIES = "application.properties";

    protected static final Path RESOURCES_FOLDER = Paths.get("src", "main", "resources");

    private static final String LOCALHOST = "localhost";
    private static final List<String> PREFIXES_TO_REPLACE = Arrays.asList(RESOURCE_PREFIX, SECRET_PREFIX);
    private static final String NATIVE_RUNNER = "-runner";
    private static final String EXE = ".exe";
    private static final String JVM_RUNNER = "-runner.jar";
    private static final String QUARKUS_APP = "quarkus-app";
    private static final String QUARKUS_RUN = "quarkus-run.jar";
    private static final String LOG_OUTPUT_FILE = "out.log";
    private static final String BUILD_TIME_PROPERTIES = "/build-time-list";
    private static final Path TEST_RESOURCES_FOLDER = Paths.get("src", "test", "resources");
    private static final Set<String> BUILD_PROPERTIES = FileUtils.loadFile(BUILD_TIME_PROPERTIES).lines()
            .collect(toSet());
    private static final String DEPENDENCY_SCOPE_DEFAULT = "compile";
    private static final String QUARKUS_GROUP_ID_DEFAULT = "io.quarkus";
    private static final int DEPENDENCY_DIRECT_FLAG = 0b000010;
    private static final String JAVA = "java";

    private final String propertiesFile;

    private Class<?>[] appClasses;
    private List<AppDependency> forcedDependencies = Collections.emptyList();
    private boolean requiresCustomBuild = false;
    private Map<String, String> propertiesSnapshot;
    private Path artifact;
    private File logOutputFile;
    private Process process;
    private LoggingHandler loggingHandler;
    private int assignedHttpPort;

    public ProdModeBootstrapQuarkusManagedResource(String propertiesFile, Class<?>[] classes,
            Dependency[] forcedDependencies) {
        this.propertiesFile = propertiesFile;

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
                String version = StringUtils.defaultIfEmpty(resolveProperty(d.version()), Version.getVersion());
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
    }

    @Override
    public void start() {
        if (process != null && process.isAlive()) {
            // do nothing
            return;
        }

        try {
            assignPorts();
            List<String> command = prepareCommand(getPropertiesForCommand());
            Log.info(context.getOwner(), "Running command: %s", String.join(" ", command));

            ProcessBuilder pb = ProcessBuilderProvider.command(command).redirectErrorStream(true)
                    .redirectOutput(logOutputFile).directory(getApplicationFolder().toFile());

            process = pb.start();

            loggingHandler = new FileServiceLoggingHandler(context.getOwner(), logOutputFile);
            loggingHandler.startWatching();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if (loggingHandler != null) {
            loggingHandler.stopWatching();
        }

        ProcessUtils.destroy(process);
    }

    @Override
    public String getHost() {
        return LOCALHOST;
    }

    @Override
    public int getMappedPort(int port) {
        return assignedHttpPort;
    }

    @Override
    public boolean isRunning() {
        return process != null && process.isAlive() && super.isRunning();
    }

    @Override
    public String getProperty(String name) {
        Path applicationProperties = getComputedApplicationProperties();
        if (!Files.exists(applicationProperties)) {
            // computed properties have not been propagated yet, we use the one from src/main/resources
            applicationProperties = RESOURCES_FOLDER.resolve(propertiesFile);
        }

        if (!Files.exists(applicationProperties)) {
            return null;
        }

        Map<String, String> computedProperties = PropertiesUtils.toMap(applicationProperties);
        return Optional.ofNullable(computedProperties.get(name))
                .orElseGet(() -> computedProperties.get(propertyWithProfile(name)));
    }

    @Override
    protected LoggingHandler getLoggingHandler() {
        return loggingHandler;
    }

    private List<String> prepareCommand(List<String> systemProperties) {
        List<String> command = new LinkedList<>();
        if (artifact.getFileName().toString().endsWith(".jar")) {
            command.add(JAVA);
            command.addAll(systemProperties);
            command.add("-jar");
            command.add(artifact.toAbsolutePath().toString());
        } else {
            command.add(artifact.toAbsolutePath().toString());
            command.addAll(systemProperties);
        }

        return command;
    }

    public boolean containsBuildProperties() {
        Map<String, String> differenceProperties = MapUtils.difference(context.getOwner().getProperties(),
                propertiesSnapshot);
        Set<String> properties = differenceProperties.keySet();
        if (properties.isEmpty()) {
            return false;
        }

        return properties.stream().anyMatch(this::isBuildProperty);
    }

    public Map<String, String> createSnapshotOfBuildProperties() {
        propertiesSnapshot = new HashMap<>(context.getOwner().getProperties());
        return new HashMap<>(context.getOwner().getProperties());
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        configureLogging();
        this.logOutputFile = new File(context.getServiceFolder().resolve(LOG_OUTPUT_FILE).toString());
        // build artifact
        copyResourcesToAppFolder();
        tryToReuseOrBuildArtifact();
    }

    private List<String> getPropertiesForCommand() {
        Map<String, String> runtimeProperties = new HashMap<>(context.getOwner().getProperties());
        runtimeProperties.putIfAbsent(QUARKUS_HTTP_PORT_PROPERTY, "" + assignedHttpPort);

        return runtimeProperties.entrySet().stream().map(e -> "-D" + e.getKey() + "=" + getComputedValue(e.getValue()))
                .collect(Collectors.toList());
    }

    private String getComputedValue(String value) {
        for (String prefix : PREFIXES_TO_REPLACE) {
            if (value.startsWith(prefix)) {
                return StringUtils.removeStart(value, prefix);
            }
        }

        return value;
    }

    private void configureLogging() {
        context.getOwner().withProperty("quarkus.log.console.format", "%d{HH:mm:ss,SSS} %s%e%n");
    }

    private void copyResourcesToAppFolder() {
        copyResourcesInFolderToAppFolder(RESOURCES_FOLDER);
        copyResourcesInFolderToAppFolder(TEST_RESOURCES_FOLDER);
        createComputedApplicationProperties();
    }

    private Path getApplicationFolder() {
        return context.getServiceFolder();
    }

    private Path getResourcesApplicationFolder() {
        return getApplicationFolder();
    }

    private Path getComputedApplicationProperties() {
        return getResourcesApplicationFolder().resolve(APPLICATION_PROPERTIES);
    }

    private void tryToReuseOrBuildArtifact() {
        Optional<String> artifactLocation = Optional.empty();
        if (!containsBuildProperties() && !requiresCustomBuild) {
            if (QuarkusUtils.isNativePackageType(context)) {
                String nativeRunnerExpectedLocation = NATIVE_RUNNER;
                if (OS.WINDOWS.isCurrentOs()) {
                    nativeRunnerExpectedLocation += EXE;
                }

                artifactLocation = findFile(TARGET, nativeRunnerExpectedLocation);

            } else {
                artifactLocation = findFile(TARGET, JVM_RUNNER)
                        .or(() -> findFile(TARGET.resolve(QUARKUS_APP), QUARKUS_RUN));
            }
        }

        if (artifactLocation.isEmpty()) {
            this.artifact = buildArtifact();
        } else {
            this.artifact = Path.of(artifactLocation.get());
        }
    }

    private Path buildArtifact() {
        try {
            createSnapshotOfBuildProperties();
            Path appFolder = getApplicationFolder();

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
            fail("Failed to build Quarkus artifacts. Caused by " + ex);
        }

        return null;
    }

    private void createComputedApplicationProperties() {
        Path sourceApplicationProperties = getResourcesApplicationFolder().resolve(propertiesFile);
        Path generatedApplicationProperties = getResourcesApplicationFolder().resolve(APPLICATION_PROPERTIES);
        Map<String, String> map = new HashMap<>();
        // Add the content of the source application properties into the auto-generated application.properties
        if (Files.exists(sourceApplicationProperties)) {
            map.putAll(PropertiesUtils.toMap(sourceApplicationProperties));
        }

        // Then add the service properties
        map.putAll(context.getOwner().getProperties());
        // Then overwrite the application properties with the generated application.properties
        PropertiesUtils.fromMap(map, generatedApplicationProperties);
    }

    private boolean isBuildProperty(String name) {
        return BUILD_PROPERTIES.stream().anyMatch(build -> name.matches(build) // It's a regular expression
                || (build.endsWith(".") && name.startsWith(build)) // contains with
                || name.equals(build)); // or it's equal to
    }

    private void copyResourcesInFolderToAppFolder(Path folder) {
        try (Stream<Path> binariesFound = Files.find(folder, Integer.MAX_VALUE,
                (path, basicFileAttributes) -> !Files.isDirectory(path))) {
            binariesFound.forEach(path -> {
                File fileToCopy = path.toFile();

                Path source = folder.relativize(path).getParent();
                Path target = getResourcesApplicationFolder();
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

    private String propertyWithProfile(String name) {
        return "%" + context.getScenarioContext().getRunningTestClassName() + "." + name;
    }

    private void assignPorts() {
        assignedHttpPort = getOrAssignPortByProperty(QUARKUS_HTTP_PORT_PROPERTY);
    }

    private int getOrAssignPortByProperty(String property) {
        return context.getOwner().getProperty(property).filter(StringUtils::isNotEmpty).map(Integer::parseInt)
                .orElseGet(SocketUtils::findAvailablePort);
    }

}
