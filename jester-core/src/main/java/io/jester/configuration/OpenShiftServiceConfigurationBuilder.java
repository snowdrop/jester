package io.jester.configuration;

import java.util.Optional;

import io.jester.core.JesterContext;

public final class OpenShiftServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.jester.api.OpenShiftServiceConfiguration, OpenShiftServiceConfiguration> {

    private static final String DEPLOYMENT_TEMPLATE_PROPERTY = "openshift.template";
    private static final String USE_INTERNAL_SERVICE_PROPERTY = "openshift.use-internal-service";
    private static final String ADDITIONAL_PORTS_PROPERTY = "openshift.additional-ports";

    @Override
    public OpenShiftServiceConfiguration build() {
        OpenShiftServiceConfiguration serviceConfiguration = new OpenShiftServiceConfiguration();
        loadString(DEPLOYMENT_TEMPLATE_PROPERTY, a -> a.template()).ifPresent(serviceConfiguration::setTemplate);
        loadBoolean(USE_INTERNAL_SERVICE_PROPERTY, a -> a.useInternalService())
                .ifPresent(serviceConfiguration::setUseInternalService);
        loadArrayOfIntegers(ADDITIONAL_PORTS_PROPERTY, a -> a.additionalPorts())
                .ifPresent(serviceConfiguration::setAdditionalPorts);
        return serviceConfiguration;
    }

    @Override
    protected Optional<io.jester.api.OpenShiftServiceConfiguration> getAnnotationConfig(String serviceName,
            JesterContext context) {
        return context.getAnnotatedConfiguration(io.jester.api.OpenShiftServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
