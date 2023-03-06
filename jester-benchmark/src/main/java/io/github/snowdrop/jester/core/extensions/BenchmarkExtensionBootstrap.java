package io.github.snowdrop.jester.core.extensions;

import io.github.snowdrop.jester.api.extensions.ExtensionBootstrap;
import io.github.snowdrop.jester.configuration.BenchmarkConfigurationBuilder;
import io.github.snowdrop.jester.core.EnableBenchmark;
import io.github.snowdrop.jester.core.JesterContext;

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
