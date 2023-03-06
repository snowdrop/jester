package io.github.jester.core.extensions;

import io.github.jester.api.extensions.ExtensionBootstrap;
import io.github.jester.configuration.BenchmarkConfigurationBuilder;
import io.github.jester.core.EnableBenchmark;
import io.github.jester.core.JesterContext;

public class BenchmarkExtensionBootstrap implements ExtensionBootstrap {

    public static final String CONFIGURATION = "benchmark";

    @Override
    public boolean appliesFor(JesterContext context) {
        return EnableBenchmark.class.isAssignableFrom(context.getTestContext().getRequiredTestClass());
    }

    @Override
    public void beforeAll(JesterContext context) {
        context.loadCustomConfiguration(CONFIGURATION, new BenchmarkConfigurationBuilder());
    }
}
