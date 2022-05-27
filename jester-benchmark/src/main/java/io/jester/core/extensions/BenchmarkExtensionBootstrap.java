package io.jester.core.extensions;

import io.jester.api.extensions.ExtensionBootstrap;
import io.jester.configuration.BenchmarkConfigurationBuilder;
import io.jester.core.EnableBenchmark;
import io.jester.core.JesterContext;

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
