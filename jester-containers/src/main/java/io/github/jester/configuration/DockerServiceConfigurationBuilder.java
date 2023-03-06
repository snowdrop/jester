package io.github.jester.configuration;

import java.util.Optional;

import io.github.jester.core.JesterContext;

public final class DockerServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.github.jester.api.DockerServiceConfiguration, DockerServiceConfiguration> {

    private static final String PRIVILEGED_MODE = "docker.privileged-mode";

    @Override
    public DockerServiceConfiguration build() {
        DockerServiceConfiguration config = new DockerServiceConfiguration();
        loadBoolean(PRIVILEGED_MODE, a -> a.privileged()).ifPresent(config::setPrivileged);
        return config;
    }

    @Override
    protected Optional<io.github.jester.api.DockerServiceConfiguration> getAnnotationConfig(String serviceName,
            JesterContext context) {
        return context.getAnnotatedConfiguration(io.github.jester.api.DockerServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
