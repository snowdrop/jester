package io.jcloud.utils;

import org.testcontainers.shaded.com.github.dockerjava.core.DefaultDockerClientConfig;
import org.testcontainers.shaded.com.github.dockerjava.core.DockerClientImpl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;

public final class DockerUtils {

    private static final Object LOCK = new Object();

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
