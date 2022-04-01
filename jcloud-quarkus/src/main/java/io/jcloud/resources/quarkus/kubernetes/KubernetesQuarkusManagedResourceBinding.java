package io.jcloud.resources.quarkus.kubernetes;

import io.jcloud.api.Quarkus;
import io.jcloud.api.extensions.QuarkusManagedResourceBinding;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ScenarioContext;
import io.jcloud.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesQuarkusManagedResourceBinding implements QuarkusManagedResourceBinding {
    @Override
    public boolean appliesFor(ScenarioContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Quarkus metadata) {
        return new ContainerRegistryProdModeBootstrapQuarkusManagedResource(metadata.location(), metadata.classes(),
                metadata.dependencies());
    }
}
