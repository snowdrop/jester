package io.jcloud.utils;

import static java.util.regex.Pattern.quote;

import java.nio.file.Path;

import io.jcloud.core.ServiceContext;

public final class DockerUtils {
    public static final String CONTAINER_REGISTRY_URL_PROPERTY = "ts.container.registry-url";
    /**
     * This value is the default registry when setting up Kind with Registry. It's convenient to set it as default to
     * run tests directly from the IDE.
     */
    public static final String DEFAULT_CONTAINER_REGISTRY_URL = "localhost:5000";

    private static final String DOCKER = "docker";
    private static final String DOCKERFILE = "Dockerfile";

    private DockerUtils() {

    }

    public static String createImageAndPush(ServiceContext service, Path runner) {
        return createImageAndPush(service, "/" + DOCKERFILE, runner);
    }

    public static String createImageAndPush(ServiceContext service, String dockerfile, Path runner) {
        String dockerfileContent = FileUtils.loadFile(dockerfile)
                .replaceAll(quote("${JAVA_VERSION}"), "" + getJavaVersion())
                .replaceAll(quote("${RUNNER_PARENT}"), runner.getParent().toString())
                .replaceAll(quote("${RUNNER}"), runner.toString());

        Path dockerfilePath = FileUtils.copyContentTo(dockerfileContent,
                service.getServiceFolder().resolve(DOCKERFILE));
        buildService(service, dockerfilePath);
        return pushToContainerRegistryUrl(service);
    }

    private static void buildService(ServiceContext service, Path dockerFile) {
        try {
            new Command(DOCKER, "build", "-f", dockerFile.toString(), "-t", getUniqueName(service), ".").runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build image " + service.getServiceFolder().toAbsolutePath(), e);
        }
    }

    private static String pushToContainerRegistryUrl(ServiceContext service) {
        String containerRegistryUrl = System.getProperty(CONTAINER_REGISTRY_URL_PROPERTY,
                DEFAULT_CONTAINER_REGISTRY_URL);
        try {
            String targetImage = containerRegistryUrl + "/" + getUniqueName(service);
            new Command(DOCKER, "tag", getUniqueName(service), targetImage).runAndWait();
            new Command(DOCKER, "push", targetImage).runAndWait();
            return targetImage;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to push image " + service.getOwner().getName() + " into " + containerRegistryUrl, e);
        }
    }

    private static String getUniqueName(ServiceContext service) {
        String uniqueName = service.getTestContext().getRequiredTestClass().getName() + "." + service.getName();
        return uniqueName.toLowerCase();
    }

    private static String getJavaVersion() {
        return System.getProperty("java.specification.version", "8");
    }
}
