package io.jcloud.utils;

import static java.util.stream.Collectors.toSet;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.jcloud.api.model.QuarkusLaunchMode;
import io.jcloud.configuration.PropertyLookup;
import io.jcloud.core.ServiceContext;
import io.quarkus.builder.Version;

public final class QuarkusUtils {

    public static final String APPLICATION_PROPERTIES = "application.properties";
    public static final Path RESOURCES_FOLDER = Paths.get("src", "main", "resources");
    public static final Path TEST_RESOURCES_FOLDER = Paths.get("src", "test", "resources");
    public static final PropertyLookup PLATFORM_GROUP_ID = new PropertyLookup("quarkus.platform.group-id",
            "io.quarkus");
    public static final PropertyLookup PLATFORM_VERSION = new PropertyLookup("quarkus.platform.version");
    public static final PropertyLookup PLUGIN_VERSION = new PropertyLookup("quarkus-plugin.version");
    public static final String PACKAGE_TYPE_NAME = "quarkus.package.type";
    public static final String MUTABLE_JAR = "mutable-jar";
    public static final PropertyLookup PACKAGE_TYPE = new PropertyLookup(PACKAGE_TYPE_NAME);
    public static final List<String> PACKAGE_TYPE_NATIVE_VALUES = Arrays.asList("native", "native-sources");
    public static final List<String> PACKAGE_TYPE_LEGACY_JAR_VALUES = Arrays.asList("legacy-jar", "uber-jar",
            "mutable-jar");
    public static final List<String> PACKAGE_TYPE_JVM_VALUES = Arrays.asList("fast-jar", "jar");
    public static final String QUARKUS_HTTP_PORT_PROPERTY = "quarkus.http.port";
    public static final int HTTP_PORT_DEFAULT = 8080;
    public static final PropertyLookup QUARKUS_JVM_S2I = new PropertyLookup("quarkus.s2i.base-jvm-image",
            "registry.access.redhat.com/ubi8/openjdk-11:latest");
    public static final PropertyLookup QUARKUS_NATIVE_S2I = new PropertyLookup("quarkus.s2i.base-native-image",
            "quay.io/quarkus/ubi-quarkus-native-binary-s2i:1.0");
    public static final String BUILD_TIME_PROPERTIES = "/build-time-list";
    public static final Set<String> BUILD_PROPERTIES = FileUtils.loadFile(BUILD_TIME_PROPERTIES).lines()
            .collect(toSet());
    private static final String DOCKERFILE_TEMPLATE = "/Dockerfile.%s";

    private QuarkusUtils() {

    }

    public static String getVersion() {
        return defaultVersionIfEmpty(PLATFORM_VERSION.get());
    }

    public static String getPluginVersion() {
        return defaultVersionIfEmpty(PLUGIN_VERSION.get());
    }

    public static boolean isNativePackageType() {
        return PACKAGE_TYPE_NATIVE_VALUES.contains(PACKAGE_TYPE.get());
    }

    public static boolean isNativePackageType(ServiceContext context) {
        return PACKAGE_TYPE_NATIVE_VALUES.contains(PACKAGE_TYPE.get(context));
    }

    public static boolean isLegacyJarPackageType(ServiceContext context) {
        return PACKAGE_TYPE_LEGACY_JAR_VALUES.contains(PACKAGE_TYPE.get(context));
    }

    public static boolean isJvmPackageType(ServiceContext context) {
        return PACKAGE_TYPE_JVM_VALUES.contains(PACKAGE_TYPE.get(context));
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

    private static String defaultVersionIfEmpty(String version) {
        if (StringUtils.isEmpty(version)) {
            version = Version.getVersion();
        }

        return version;
    }

    public static String getDockerfile(QuarkusLaunchMode mode) {
        return String.format(DOCKERFILE_TEMPLATE, mode.getName());
    }
}
