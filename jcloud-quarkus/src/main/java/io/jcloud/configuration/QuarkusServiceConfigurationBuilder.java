package io.jcloud.configuration;

import java.util.Optional;

import io.jcloud.core.ScenarioContext;

public final class QuarkusServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.jcloud.api.QuarkusServiceConfiguration, QuarkusServiceConfiguration> {

    private static final String EXPECTED_OUTPUT = "quarkus.expected-log";

    @Override
    public QuarkusServiceConfiguration build() {
        QuarkusServiceConfiguration config = new QuarkusServiceConfiguration();
        loadString(EXPECTED_OUTPUT, a -> a.expectedLog()).ifPresent(config::setExpectedLog);
        return config;
    }

    @Override
    protected Optional<io.jcloud.api.QuarkusServiceConfiguration> getAnnotationConfig(String serviceName,
            ScenarioContext scenarioContext) {
        return scenarioContext.getAnnotatedConfiguration(io.jcloud.api.QuarkusServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
