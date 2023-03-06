package io.github.jester.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.quarkus.bootstrap.BootstrapConstants;

/**
 * This is a copy of PathTestHelper from Quarkus source code.
 */
public final class PathTestHelper {
    private static final Map<String, String> TEST_TO_MAIN_DIR_FRAGMENTS = new HashMap<>();

    static {
        // region Eclipse
        TEST_TO_MAIN_DIR_FRAGMENTS.put("bin" + File.separator + "test", "bin" + File.separator + "main");
        // endregion

        // region Idea
        TEST_TO_MAIN_DIR_FRAGMENTS.put("out" + File.separator + "test", "out" + File.separator + "production");
        // endregion

        // region Gradle
        // region Java
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "java" + File.separator + "native-test",
                "classes" + File.separator + "java" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "java" + File.separator + "test",
                "classes" + File.separator + "java" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "java" + File.separator + "integration-test",
                "classes" + File.separator + "java" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "java" + File.separator + "integrationTest",
                "classes" + File.separator + "java" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "java" + File.separator + "native-integrationTest",
                "classes" + File.separator + "java" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "java" + File.separator + "native-integration-test",
                "classes" + File.separator + "java" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put(
                // synthetic tmp dirs when there are multiple outputs
                "quarkus-app-classes-test", "quarkus-app-classes");
        // endregion
        // region Kotlin
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "kotlin" + File.separator + "native-test",
                "classes" + File.separator + "kotlin" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "kotlin" + File.separator + "test",
                "classes" + File.separator + "kotlin" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "kotlin" + File.separator + "integration-test",
                "classes" + File.separator + "kotlin" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "kotlin" + File.separator + "integrationTest",
                "classes" + File.separator + "kotlin" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put(
                "classes" + File.separator + "kotlin" + File.separator + "native-integrationTest",
                "classes" + File.separator + "kotlin" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put(
                "classes" + File.separator + "kotlin" + File.separator + "native-integration-test",
                "classes" + File.separator + "kotlin" + File.separator + "main");
        // endregion
        // region Scala
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "scala" + File.separator + "native-test",
                "classes" + File.separator + "scala" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "scala" + File.separator + "test",
                "classes" + File.separator + "scala" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "scala" + File.separator + "integration-test",
                "classes" + File.separator + "scala" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "scala" + File.separator + "integrationTest",
                "classes" + File.separator + "scala" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put("classes" + File.separator + "scala" + File.separator + "native-integrationTest",
                "classes" + File.separator + "scala" + File.separator + "main");
        TEST_TO_MAIN_DIR_FRAGMENTS.put(
                "classes" + File.separator + "scala" + File.separator + "native-integration-test",
                "classes" + File.separator + "scala" + File.separator + "main");
        // endregion
        // endregion

        // region Maven
        TEST_TO_MAIN_DIR_FRAGMENTS.put(File.separator + "test-classes", File.separator + "classes");
        // endregion

        String mappings = System.getenv(BootstrapConstants.TEST_TO_MAIN_MAPPINGS);
        if (mappings != null) {
            Stream.of(mappings.split(",")).filter(s -> !s.isEmpty()).forEach(s -> {
                String[] entry = s.split(":");
                if (entry.length == 2) {
                    TEST_TO_MAIN_DIR_FRAGMENTS.put(entry[0], entry[1]);
                } else {
                    throw new IllegalStateException("Unable to parse additional test-to-main mapping: " + s);
                }
            });
        }
    }

    private PathTestHelper() {
    }

    /**
     * Resolves the directory or the JAR file containing the test class.
     *
     * @param testClass
     *            the test class
     *
     * @return directory or JAR containing the test class
     */
    public static Path getTestClassesLocation(Class<?> testClass) {
        String classFileName = testClass.getName().replace('.', File.separatorChar) + ".class";
        URL resource = testClass.getClassLoader().getResource(testClass.getName().replace('.', '/') + ".class");

        if (resource.getProtocol().equals("jar")) {
            try {
                resource = URI.create(resource.getFile().substring(0, resource.getFile().indexOf('!'))).toURL();
                return toPath(resource);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to resolve the location of the JAR containing " + testClass, e);
            }
        }

        if (!isInTestDir(resource)) {
            throw new RuntimeException("The test class " + testClass + " is not located in any of the directories "
                    + TEST_TO_MAIN_DIR_FRAGMENTS.keySet());
        }

        Path path = toPath(resource);
        return path.getRoot().resolve(path.subpath(0, path.getNameCount() - Paths.get(classFileName).getNameCount()));
    }

    private static boolean isInTestDir(URL resource) {
        String path = toPath(resource).toString();
        return TEST_TO_MAIN_DIR_FRAGMENTS.keySet().stream().anyMatch(path::contains);
    }

    private static Path toPath(URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to translate " + url + " to local path", e);
        }
    }
}
