package io.github.jester.configuration;

import java.util.Optional;

import io.github.jester.core.JesterContext;

public final class BenchmarkConfigurationBuilder
        extends BaseConfigurationBuilder<io.github.jester.api.BenchmarkConfiguration, BenchmarkConfiguration> {

    private static final String OUTPUT_LOCATION = "benchmark.output-location";

    @Override
    public BenchmarkConfiguration build() {
        BenchmarkConfiguration config = new BenchmarkConfiguration();
        loadString(OUTPUT_LOCATION, a -> a.outputLocation()).ifPresent(config::setOutputLocation);
        return config;
    }

    @Override
    protected Optional<io.github.jester.api.BenchmarkConfiguration> getAnnotationConfig(String serviceName,
            JesterContext context) {
        return context.getAnnotatedConfiguration(io.github.jester.api.BenchmarkConfiguration.class);
    }
}
