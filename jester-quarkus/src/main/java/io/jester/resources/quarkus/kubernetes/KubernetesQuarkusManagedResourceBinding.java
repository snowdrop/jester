package io.jester.resources.quarkus.kubernetes;

import io.jester.api.Quarkus;
import io.jester.api.extensions.QuarkusManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesQuarkusManagedResourceBinding implements QuarkusManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Quarkus metadata) {
        return new ContainerRegistryProdModeBootstrapQuarkusManagedResource(metadata.location(), metadata.classes(),
                metadata.dependencies());
    }
}
