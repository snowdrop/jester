package io.jcloud.configuration;

import java.util.Optional;

import io.jcloud.core.JCloudContext;

public final class KubernetesConfigurationBuilder
        extends BaseConfigurationBuilder<io.jcloud.api.RunOnKubernetes, KubernetesConfiguration> {

    private static final String PRINT_INFO_ON_ERROR = "print.info.on.error";
    private static final String DELETE_NAMESPACE_AFTER = "delete.namespace.after.all";
    private static final String EPHEMERAL_NAMESPACE_ENABLED = "ephemeral.namespaces.enabled";
    private static final String ADDITIONAL_RESOURCES = "additional-resources";

    @Override
    public KubernetesConfiguration build() {
        KubernetesConfiguration config = new KubernetesConfiguration();
        loadBoolean(PRINT_INFO_ON_ERROR, a -> a.printInfoOnError()).ifPresent(config::setPrintInfoOnError);
        loadBoolean(DELETE_NAMESPACE_AFTER, a -> a.deleteNamespaceAfterAll())
                .ifPresent(config::setDeleteNamespaceAfterAll);
        loadBoolean(EPHEMERAL_NAMESPACE_ENABLED, a -> a.ephemeralNamespaceEnabled())
                .ifPresent(config::setEphemeralNamespaceEnabled);
        loadArrayOfStrings(ADDITIONAL_RESOURCES, a -> a.additionalResources())
                .ifPresent(config::setAdditionalResources);
        return config;
    }

    @Override
    protected Optional<io.jcloud.api.RunOnKubernetes> getAnnotationConfig(String serviceName, JCloudContext context) {
        return context.getAnnotatedConfiguration(io.jcloud.api.RunOnKubernetes.class);
    }
}
