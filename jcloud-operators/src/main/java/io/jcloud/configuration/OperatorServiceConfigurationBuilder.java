package io.jcloud.configuration;

import java.util.Optional;

import io.jcloud.core.JCloudContext;

public final class OperatorServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.jcloud.api.OperatorServiceConfiguration, OperatorServiceConfiguration> {

    private static final String INSTALL_TIMEOUT = "operator.install.timeout";

    @Override
    public OperatorServiceConfiguration build() {
        OperatorServiceConfiguration config = new OperatorServiceConfiguration();
        loadDuration(INSTALL_TIMEOUT, a -> a.installTimeout()).ifPresent(config::setInstallTimeout);
        return config;
    }

    @Override
    protected Optional<io.jcloud.api.OperatorServiceConfiguration> getAnnotationConfig(String serviceName,
            JCloudContext context) {
        return context.getAnnotatedConfiguration(io.jcloud.api.OperatorServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
