package io.github.snowdrop.jester.utils;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.model.QuarkusLaunchMode;
import io.github.snowdrop.jester.core.ServiceContext;

public final class QuarkusUtils {

    public static final String APPLICATION_PROPERTIES = "application.properties";
    public static final Path RESOURCES_FOLDER = Paths.get("src", "main", "resources");
    public static final Path TEST_RESOURCES_FOLDER = Paths.get("src", "test", "resources");
    public static final String PLATFORM_GROUP_ID = System.getProperty("quarkus.platform.group-id", "io.quarkus");
    public static final String PLATFORM_VERSION = System.getProperty("quarkus.platform.version");
    public static final String PLUGIN_VERSION = System.getProperty("quarkus-plugin.version");
    public static final String PACKAGE_TYPE_NAME = "quarkus.package.type";
    public static final String MUTABLE_JAR = "mutable-jar";
    public static final String PACKAGE_TYPE = System.getProperty(PACKAGE_TYPE_NAME);
    public static final List<String> PACKAGE_TYPE_NATIVE_VALUES = Arrays.asList("native", "native-sources");
    public static final List<String> PACKAGE_TYPE_LEGACY_JAR_VALUES = Arrays.asList("legacy-jar", "uber-jar",
            "mutable-jar");
    public static final List<String> PACKAGE_TYPE_JVM_VALUES = Arrays.asList("fast-jar", "jar");
    public static final String QUARKUS_HTTP_PORT_PROPERTY = "quarkus.http.port";
    public static final String QUARKUS_SSL_PORT_PROPERTY = "quarkus.http.ssl-port";
    public static final String QUARKUS_GRPC_SERVER_PORT = "quarkus.grpc.server.port";
    public static final String QUARKUS_KUBERNETES_SPI_CUSTOM_PROJECT = "io.quarkus.kubernetes.spi.CustomProjectRootBuildItem";
    public static final int HTTP_PORT_DEFAULT = 8080;
    public static final String QUARKUS_JVM_S2I = System.getProperty("quarkus.s2i.base-jvm-image",
            "registry.access.redhat.com/ubi8/openjdk-11:latest");
    public static final String QUARKUS_NATIVE_S2I = System.getProperty("quarkus.s2i.base-native-image",
            "quay.io/quarkus/ubi-quarkus-native-binary-s2i:1.0");
    public static final String BUILD_TIME_PROPERTIES = "/build-time-list";
    public static final Set<String> BUILD_PROPERTIES = FileUtils.loadFile(BUILD_TIME_PROPERTIES).lines()
            .collect(toSet());
    public static final String KUBERNETES_OUTPUT_DIRECTORY = "quarkus.kubernetes.output-directory";
    public static final String KUBERNETES_OUTPUT_DIRECTORY_DEFAULT = "kubernetes";

    private static final String VERSION;
    private static final String DOCKERFILE_TEMPLATE = "/Dockerfile.%s";

    static {
        String versionString = "(unknown)";

        try {
            versionString = (String) ReflectionUtils.invokeStaticMethod("io.quarkus.builder.Version", "getVersion");
        } catch (Exception ex) {

        }

        VERSION = versionString;
    }

    private QuarkusUtils() {

    }

    public static String getVersion() {
        return StringUtils.defaultString(PLATFORM_VERSION, VERSION);
    }

    public static String getPluginVersion() {
        return StringUtils.defaultString(PLUGIN_VERSION, VERSION);
    }

    public static boolean isNativePackageType() {
        return PACKAGE_TYPE_NATIVE_VALUES.contains(PACKAGE_TYPE);
    }

    public static boolean isNativePackageType(Service service) {
        return isNativePackageType()
                || service.getProperty(PACKAGE_TYPE_NAME).map(PACKAGE_TYPE_NATIVE_VALUES::contains).orElse(false);
    }

    public static boolean isLegacyJarPackageType() {
        return PACKAGE_TYPE_LEGACY_JAR_VALUES.contains(PACKAGE_TYPE);
    }

    public static boolean isLegacyJarPackageType(Service service) {
        return isLegacyJarPackageType()
                || service.getProperty(PACKAGE_TYPE_NAME).map(PACKAGE_TYPE_LEGACY_JAR_VALUES::contains).orElse(false);
    }

    public static boolean isQuarkusVersion2Dot3OrAbove() {
        String quarkusVersion = getVersion();
        return !quarkusVersion.startsWith("2.2.") && !quarkusVersion.startsWith("2.1.")
                && !quarkusVersion.startsWith("2.0.") && !quarkusVersion.startsWith("1.");
    }

    public static boolean isBuildProperty(String name) {
        return BUILD_PROPERTIES.stream().anyMatch(build -> name.matches(build) // It's a regular expression
                || (build.endsWith(".") && name.startsWith(build)) // contains with
                || name.equals(build)); // or it's equal to
    }

    public static String getDockerfile(QuarkusLaunchMode mode) {
        return String.format(DOCKERFILE_TEMPLATE, mode.getName());
    }

    public static boolean isBootstrapDependencyAdded() {
        return ReflectionUtils.loadClass("io.quarkus.bootstrap.app.QuarkusBootstrap").isPresent();
    }

    public static String getKubernetesFolder(Service service) {
        return service.getProperty(KUBERNETES_OUTPUT_DIRECTORY, KUBERNETES_OUTPUT_DIRECTORY_DEFAULT);
    }

    public static boolean isKubernetesExtensionLoaded() {
        return ReflectionUtils.loadClass("io.quarkus.kubernetes.runtime.devui.KubernetesManifestService").isPresent();
    }

    public static void copyResourcesToServiceFolder(Path location, ServiceContext context) {
        copyResourcesInFolderToAppFolder(location.resolve(RESOURCES_FOLDER), context);
        copyResourcesInFolderToAppFolder(TEST_RESOURCES_FOLDER, context);
        createComputedApplicationProperties(context);
    }

    private static void copyResourcesInFolderToAppFolder(Path folder, ServiceContext context) {
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

    private static void createComputedApplicationProperties(ServiceContext context) {
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
}
