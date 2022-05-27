package io.jester.configuration;

import java.util.Optional;

import io.jester.core.JesterContext;

public final class BenchmarkConfigurationBuilder
        extends BaseConfigurationBuilder<io.jester.api.BenchmarkConfiguration, BenchmarkConfiguration> {

    private static final String OUTPUT_LOCATION = "benchmark.output-location";

    @Override
    public BenchmarkConfiguration build() {
        BenchmarkConfiguration config = new BenchmarkConfiguration();
        loadString(OUTPUT_LOCATION, a -> a.outputLocation()).ifPresent(config::setOutputLocation);
        return config;
    }

    @Override
    protected Optional<io.jester.api.BenchmarkConfiguration> getAnnotationConfig(String serviceName,
            JesterContext context) {
        return context.getAnnotatedConfiguration(io.jester.api.BenchmarkConfiguration.class);
    }
}
