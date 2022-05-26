package io.jester.resources.spring.kubernetes;

import io.jester.api.Spring;
import io.jester.api.extensions.SpringManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesSpringManagedResourceBinding implements SpringManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Spring metadata) {
        return new ContainerRegistrySpringManagedResource(metadata.location(), metadata.forceBuild(),
                metadata.buildCommands());
    }
}
