package io.jcloud.configuration;

import java.util.Optional;

import io.jcloud.core.JCloudContext;

public final class SpringServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.jcloud.api.SpringServiceConfiguration, SpringServiceConfiguration> {

    private static final String EXPECTED_OUTPUT = "spring.expected-log";

    @Override
    public SpringServiceConfiguration build() {
        SpringServiceConfiguration config = new SpringServiceConfiguration();
        loadString(EXPECTED_OUTPUT, a -> a.expectedLog()).ifPresent(config::setExpectedLog);
        return config;
    }

    @Override
    protected Optional<io.jcloud.api.SpringServiceConfiguration> getAnnotationConfig(String serviceName,
            JCloudContext context) {
        return context.getAnnotatedConfiguration(io.jcloud.api.SpringServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
