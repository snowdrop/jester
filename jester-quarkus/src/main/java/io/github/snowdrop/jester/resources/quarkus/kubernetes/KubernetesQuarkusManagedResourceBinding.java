package io.github.snowdrop.jester.resources.quarkus.kubernetes;

import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.extensions.QuarkusManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.KubernetesExtensionBootstrap;

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
