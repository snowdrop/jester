package io.github.snowdrop.jester.configuration;

import java.util.Optional;

import io.github.snowdrop.jester.core.JesterContext;

public final class QuarkusServiceConfigurationBuilder extends
        BaseConfigurationBuilder<io.github.snowdrop.jester.api.QuarkusServiceConfiguration, QuarkusServiceConfiguration> {

    private static final String EXPECTED_OUTPUT = "quarkus.expected-log";

    @Override
    public QuarkusServiceConfiguration build() {
        QuarkusServiceConfiguration config = new QuarkusServiceConfiguration();
        loadString(EXPECTED_OUTPUT, a -> a.expectedLog()).ifPresent(config::setExpectedLog);
        return config;
    }

    @Override
    protected Optional<io.github.snowdrop.jester.api.QuarkusServiceConfiguration> getAnnotationConfig(
            String serviceName, JesterContext context) {
        return context.getAnnotatedConfiguration(io.github.snowdrop.jester.api.QuarkusServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
