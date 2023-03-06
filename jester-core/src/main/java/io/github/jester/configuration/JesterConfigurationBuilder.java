package io.github.jester.configuration;

import java.util.Optional;

import io.github.jester.api.Jester;
import io.github.jester.core.JesterContext;

public final class JesterConfigurationBuilder extends BaseConfigurationBuilder<Jester, JesterConfiguration> {

    private static final String TARGET = "target";
    private static final String ENABLE_PROFILING = "enable.profiling";

    @Override
    public JesterConfiguration build() {
        JesterConfiguration config = new JesterConfiguration();
        loadString(TARGET, a -> a.target()).ifPresent(config::setTarget);
        loadBoolean(ENABLE_PROFILING, a -> a.enableProfiling()).ifPresent(config::setProfilingEnabled);
        return config;
    }

    @Override
    protected Optional<Jester> getAnnotationConfig(String serviceName, JesterContext context) {
        return context.getAnnotatedConfiguration(Jester.class);
    }
}
