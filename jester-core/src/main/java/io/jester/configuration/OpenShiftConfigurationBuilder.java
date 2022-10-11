package io.jester.configuration;

import java.util.Optional;

import io.jester.core.JesterContext;

public final class OpenShiftConfigurationBuilder
        extends BaseConfigurationBuilder<io.jester.api.RunOnOpenShift, OpenShiftConfiguration> {

    private static final String PRINT_INFO_ON_ERROR = "print.info.on.error";
    private static final String DELETE_NAMESPACE_AFTER = "delete.namespace.after.all";
    private static final String EPHEMERAL_NAMESPACE_ENABLED = "ephemeral.namespaces.enabled";
    private static final String ADDITIONAL_RESOURCES = "additional-resources";

    @Override
    public OpenShiftConfiguration build() {
        OpenShiftConfiguration config = new OpenShiftConfiguration();
        loadBoolean(PRINT_INFO_ON_ERROR, a -> a.printInfoOnError()).ifPresent(config::setPrintInfoOnError);
        loadBoolean(DELETE_NAMESPACE_AFTER, a -> a.deleteProjectAfterAll()).ifPresent(config::setDeleteProjectAfterAll);
        loadBoolean(EPHEMERAL_NAMESPACE_ENABLED, a -> a.ephemeralStorageEnabled())
                .ifPresent(config::setEphemeralStorageEnabled);
        loadArrayOfStrings(ADDITIONAL_RESOURCES, a -> a.additionalResources())
                .ifPresent(config::setAdditionalResources);
        return config;
    }

    @Override
    protected Optional<io.jester.api.RunOnOpenShift> getAnnotationConfig(String serviceName, JesterContext context) {
        return context.getAnnotatedConfiguration(io.jester.api.RunOnOpenShift.class);
    }
}
