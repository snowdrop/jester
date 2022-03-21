package io.jcloud.utils;

import static java.util.regex.Pattern.quote;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.jcloud.core.ServiceContext;

public final class DockerUtils {
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
        String image = build(service, dockerfilePath.toString(), Paths.get("").toString());
        push(service);
        return image;
    }

    public static String build(ServiceContext service, String dockerfile, String directory) {
        String image = service.getConfiguration().getImageRegistry() + "/" + getUniqueName(service);
        try {
            new Command(DOCKER, "build", "-f", Path.of(dockerfile).toFile().getAbsoluteFile().toString(), "-t", image,
                    ".").onDirectory(directory).runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build image " + service.getServiceFolder().toAbsolutePath(), e);
        }

        return image;
    }

    public static void push(ServiceContext service) {
        try {
            new Command(DOCKER, "push", service.getConfiguration().getImageRegistry() + "/" + getUniqueName(service))
                    .runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to push image " + service.getOwner().getName() + " into "
                    + service.getConfiguration().getImageRegistry(), e);
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
