package io.jester.configuration;

import java.util.Optional;

import io.jester.core.JesterContext;

public final class SpringServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.jester.api.SpringServiceConfiguration, SpringServiceConfiguration> {

    private static final String EXPECTED_OUTPUT = "spring.expected-log";

    @Override
    public SpringServiceConfiguration build() {
        SpringServiceConfiguration config = new SpringServiceConfiguration();
        loadString(EXPECTED_OUTPUT, a -> a.expectedLog()).ifPresent(config::setExpectedLog);
        return config;
    }

    @Override
    protected Optional<io.jester.api.SpringServiceConfiguration> getAnnotationConfig(String serviceName,
            JesterContext context) {
        return context.getAnnotatedConfiguration(io.jester.api.SpringServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
