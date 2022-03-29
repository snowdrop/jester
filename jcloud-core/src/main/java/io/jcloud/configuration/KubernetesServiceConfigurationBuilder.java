package io.jcloud.configuration;

import java.util.Optional;

import io.jcloud.core.ScenarioContext;

public final class KubernetesServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.jcloud.api.KubernetesServiceConfiguration, KubernetesServiceConfiguration> {

    private static final String DEPLOYMENT_TEMPLATE_PROPERTY = "kubernetes.template";
    private static final String USE_INTERNAL_SERVICE_AS_URL_PROPERTY = "kubernetes.use-internal-service-as-url";

    @Override
    public KubernetesServiceConfiguration build() {
        KubernetesServiceConfiguration serviceConfiguration = new KubernetesServiceConfiguration();
        loadString(DEPLOYMENT_TEMPLATE_PROPERTY, a -> a.template()).ifPresent(serviceConfiguration::setTemplate);
        loadBoolean(USE_INTERNAL_SERVICE_AS_URL_PROPERTY, a -> a.useInternalService())
                .ifPresent(serviceConfiguration::setUseInternalService);
        return serviceConfiguration;
    }

    @Override
    protected Optional<io.jcloud.api.KubernetesServiceConfiguration> getAnnotationConfig(String serviceName,
            ScenarioContext scenarioContext) {
        return scenarioContext.getAnnotatedConfiguration(io.jcloud.api.KubernetesServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
