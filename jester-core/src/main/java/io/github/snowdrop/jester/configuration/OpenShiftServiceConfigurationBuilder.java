package io.github.snowdrop.jester.configuration;

import java.util.Optional;

import io.github.snowdrop.jester.core.JesterContext;

public final class OpenShiftServiceConfigurationBuilder extends
        BaseConfigurationBuilder<io.github.snowdrop.jester.api.OpenShiftServiceConfiguration, OpenShiftServiceConfiguration> {

    private static final String DEPLOYMENT_TEMPLATE_PROPERTY = "openshift.template";
    private static final String USE_INTERNAL_SERVICE_PROPERTY = "openshift.use-internal-service";
    private static final String ADDITIONAL_PORTS_PROPERTY = "openshift.additional-ports";
    private static final String USE_ROUTE = "openshift.use-route";

    @Override
    public OpenShiftServiceConfiguration build() {
        OpenShiftServiceConfiguration config = new OpenShiftServiceConfiguration();
        loadString(DEPLOYMENT_TEMPLATE_PROPERTY, a -> a.template()).ifPresent(config::setTemplate);
        loadBoolean(USE_INTERNAL_SERVICE_PROPERTY, a -> a.useInternalService())
                .ifPresent(config::setUseInternalService);
        loadArrayOfIntegers(ADDITIONAL_PORTS_PROPERTY, a -> a.additionalPorts()).ifPresent(config::setAdditionalPorts);
        loadBoolean(USE_ROUTE, a -> a.useRoute()).ifPresent(config::setUseRoute);
        return config;
    }

    @Override
    protected Optional<io.github.snowdrop.jester.api.OpenShiftServiceConfiguration> getAnnotationConfig(
            String serviceName, JesterContext context) {
        return context.getAnnotatedConfiguration(io.github.snowdrop.jester.api.OpenShiftServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
