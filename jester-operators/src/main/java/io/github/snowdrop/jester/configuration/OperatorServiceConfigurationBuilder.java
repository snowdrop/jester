package io.github.snowdrop.jester.configuration;

import java.util.Optional;

import io.github.snowdrop.jester.core.JesterContext;

public final class OperatorServiceConfigurationBuilder extends
        BaseConfigurationBuilder<io.github.snowdrop.jester.api.OperatorServiceConfiguration, OperatorServiceConfiguration> {

    private static final String INSTALL_TIMEOUT = "operator.install.timeout";

    @Override
    public OperatorServiceConfiguration build() {
        OperatorServiceConfiguration config = new OperatorServiceConfiguration();
        loadDuration(INSTALL_TIMEOUT, a -> a.installTimeout()).ifPresent(config::setInstallTimeout);
        return config;
    }

    @Override
    protected Optional<io.github.snowdrop.jester.api.OperatorServiceConfiguration> getAnnotationConfig(
            String serviceName, JesterContext context) {
        return context.getAnnotatedConfiguration(io.github.snowdrop.jester.api.OperatorServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
