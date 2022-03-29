package io.jcloud.configuration;

import java.util.Optional;

import io.jcloud.core.ScenarioContext;

public final class ScenarioConfigurationBuilder
        extends BaseConfigurationBuilder<io.jcloud.api.Scenario, ScenarioConfiguration> {

    private static final String TARGET = "target";

    @Override
    public ScenarioConfiguration build() {
        ScenarioConfiguration config = new ScenarioConfiguration();
        loadString(TARGET, a -> a.target()).ifPresent(config::setTarget);
        return config;
    }

    @Override
    protected Optional<io.jcloud.api.Scenario> getAnnotationConfig(String serviceName,
            ScenarioContext scenarioContext) {
        return scenarioContext.getAnnotatedConfiguration(io.jcloud.api.Scenario.class);
    }
}
