package io.jcloud.resources.quarkus.kubernetes;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.Quarkus;
import io.jcloud.api.RunOnKubernetes;
import io.jcloud.api.extensions.QuarkusManagedResourceBinding;
import io.jcloud.core.ManagedResource;

public class KubernetesQuarkusManagedResourceBinding implements QuarkusManagedResourceBinding {
    @Override
    public boolean appliesFor(ExtensionContext context) {
        return context.getRequiredTestClass().isAnnotationPresent(RunOnKubernetes.class);
    }

    @Override
    public ManagedResource init(Quarkus metadata) {
        return new ContainerRegistryProdModeBootstrapQuarkusManagedResource(metadata.properties(), metadata.classes(),
                metadata.dependencies());
    }
}
