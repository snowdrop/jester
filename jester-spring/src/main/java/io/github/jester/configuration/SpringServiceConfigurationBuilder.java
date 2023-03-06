package io.github.jester.configuration;

import java.util.Optional;

import io.github.jester.core.JesterContext;

public final class SpringServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.github.jester.api.SpringServiceConfiguration, SpringServiceConfiguration> {

    private static final String EXPECTED_OUTPUT = "spring.expected-log";

    @Override
    public SpringServiceConfiguration build() {
        SpringServiceConfiguration config = new SpringServiceConfiguration();
        loadString(EXPECTED_OUTPUT, a -> a.expectedLog()).ifPresent(config::setExpectedLog);
        return config;
    }

    @Override
    protected Optional<io.github.jester.api.SpringServiceConfiguration> getAnnotationConfig(String serviceName,
            JesterContext context) {
        return context.getAnnotatedConfiguration(io.github.jester.api.SpringServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
