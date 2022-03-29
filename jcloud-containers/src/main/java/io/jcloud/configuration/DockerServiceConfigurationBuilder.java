package io.jcloud.configuration;

import java.util.Optional;

import io.jcloud.core.ScenarioContext;

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
            ScenarioContext scenarioContext) {
        return scenarioContext.getAnnotatedConfiguration(io.jcloud.api.DockerServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
