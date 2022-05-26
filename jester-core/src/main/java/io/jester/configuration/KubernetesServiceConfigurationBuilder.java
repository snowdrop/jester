package io.jester.configuration;

import java.util.Optional;

import io.jester.core.JesterContext;

public final class KubernetesServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.jester.api.KubernetesServiceConfiguration, KubernetesServiceConfiguration> {

    private static final String DEPLOYMENT_TEMPLATE_PROPERTY = "kubernetes.template";
    private static final String USE_INTERNAL_SERVICE_PROPERTY = "kubernetes.use-internal-service";
    private static final String ADDITIONAL_PORTS_PROPERTY = "kubernetes.additional-ports";

    @Override
    public KubernetesServiceConfiguration build() {
        KubernetesServiceConfiguration serviceConfiguration = new KubernetesServiceConfiguration();
        loadString(DEPLOYMENT_TEMPLATE_PROPERTY, a -> a.template()).ifPresent(serviceConfiguration::setTemplate);
        loadBoolean(USE_INTERNAL_SERVICE_PROPERTY, a -> a.useInternalService())
                .ifPresent(serviceConfiguration::setUseInternalService);
        loadArrayOfIntegers(ADDITIONAL_PORTS_PROPERTY, a -> a.additionalPorts())
                .ifPresent(serviceConfiguration::setAdditionalPorts);
        return serviceConfiguration;
    }

    @Override
    protected Optional<io.jester.api.KubernetesServiceConfiguration> getAnnotationConfig(String serviceName,
            JesterContext context) {
        return context.getAnnotatedConfiguration(io.jester.api.KubernetesServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
