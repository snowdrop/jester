package io.jcloud.configuration;

import java.util.Optional;

import io.jcloud.api.JCloud;
import io.jcloud.core.JCloudContext;

public final class JCloudConfigurationBuilder extends BaseConfigurationBuilder<JCloud, JCloudConfiguration> {

    private static final String TARGET = "target";

    @Override
    public JCloudConfiguration build() {
        JCloudConfiguration config = new JCloudConfiguration();
        loadString(TARGET, a -> a.target()).ifPresent(config::setTarget);
        return config;
    }

    @Override
    protected Optional<JCloud> getAnnotationConfig(String serviceName, JCloudContext context) {
        return context.getAnnotatedConfiguration(JCloud.class);
    }
}