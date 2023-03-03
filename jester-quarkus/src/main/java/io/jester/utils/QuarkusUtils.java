package io.jester.utils;

import static java.util.stream.Collectors.toSet;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.jester.api.Service;
import io.jester.api.model.QuarkusLaunchMode;

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
}
