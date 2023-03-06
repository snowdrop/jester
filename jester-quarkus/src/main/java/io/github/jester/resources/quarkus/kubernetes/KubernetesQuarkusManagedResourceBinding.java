package io.github.jester.resources.quarkus.kubernetes;

import io.github.jester.api.Quarkus;
import io.github.jester.api.extensions.QuarkusManagedResourceBinding;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesQuarkusManagedResourceBinding implements QuarkusManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Quarkus metadata) {
        return new ContainerRegistryProdModeBootstrapQuarkusKubernetesManagedResource(metadata.location(),
                metadata.classes(), metadata.dependencies(), metadata.forceBuild(), metadata.version());
    }
}
