package io.jester.configuration;

import java.util.Optional;

import io.jester.core.JesterContext;

public final class QuarkusServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.jester.api.QuarkusServiceConfiguration, QuarkusServiceConfiguration> {

    private static final String EXPECTED_OUTPUT = "quarkus.expected-log";

    @Override
    public QuarkusServiceConfiguration build() {
        QuarkusServiceConfiguration config = new QuarkusServiceConfiguration();
        loadString(EXPECTED_OUTPUT, a -> a.expectedLog()).ifPresent(config::setExpectedLog);
        return config;
    }

    @Override
    protected Optional<io.jester.api.QuarkusServiceConfiguration> getAnnotationConfig(String serviceName,
            JesterContext context) {
        return context.getAnnotatedConfiguration(io.jester.api.QuarkusServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
