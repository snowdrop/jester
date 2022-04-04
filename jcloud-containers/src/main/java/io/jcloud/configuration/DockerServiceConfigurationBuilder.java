package io.jcloud.configuration;

import java.util.Optional;

import io.jcloud.core.JCloudContext;

public final class DockerServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.jcloud.api.DockerServiceConfiguration, DockerServiceConfiguration> {

    private static final String PRIVILEGED_MODE = "docker.privileged-mode";

    @Override
    public DockerServiceConfiguration build() {
        DockerServiceConfiguration config = new DockerServiceConfiguration();
        loadBoolean(PRIVILEGED_MODE, a -> a.privileged()).ifPresent(config::setPrivileged);
        return config;
    }

    @Override
    protected Optional<io.jcloud.api.DockerServiceConfiguration> getAnnotationConfig(String serviceName,
            JCloudContext context) {
        return context.getAnnotatedConfiguration(io.jcloud.api.DockerServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
