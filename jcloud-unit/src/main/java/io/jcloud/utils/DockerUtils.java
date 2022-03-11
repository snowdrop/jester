package io.jcloud.utils;

import java.util.Objects;
import java.util.Random;

import org.testcontainers.shaded.com.github.dockerjava.core.DefaultDockerClientConfig;
import org.testcontainers.shaded.com.github.dockerjava.core.DockerClientImpl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;

public final class DockerUtils {

    private static final String CONTAINER_PREFIX = "ts.global.docker-container-prefix";
    private static final Object LOCK = new Object();
    private static final Random RANDOM = new Random();

    private static DockerClient dockerClientInstance;

    private DockerUtils() {

    }

    /**
     * Remove docker image by image id. Example: quay.io/bitnami/consul:1.9.3
     *
     * @param imageId docker image to delete.
     */
    public static void removeImageById(String imageId) {
        dockerClient().removeImageCmd(imageId).withForce(true).exec();
    }

    /**
     * Generate a docker container name following the standard container naming rules.
     *
     * @return the generated docker container name.
     */
    public static String generateDockerContainerName() {
        String containerName = "" + (RANDOM.nextInt() & Integer.MAX_VALUE);
        String dockerContainerPrefix = System.getProperty(CONTAINER_PREFIX);
        if (Objects.nonNull(dockerContainerPrefix)) {
            containerName = dockerContainerPrefix + "-" + containerName;
        }

        return containerName;
    }

    private static DockerClient dockerClient() {
        if (dockerClientInstance == null) {
            synchronized (LOCK) {
                if (dockerClientInstance == null) {
                    DefaultDockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                            .build();
                    ZerodepDockerHttpClient dockerHttpClient = new ZerodepDockerHttpClient.Builder()
                            .dockerHost(dockerClientConfig.getDockerHost())
                            .sslConfig(dockerClientConfig.getSSLConfig()).build();
                    dockerClientInstance = DockerClientImpl.getInstance(dockerClientConfig, dockerHttpClient);
                }
            }
        }

        return dockerClientInstance;
    }
}
