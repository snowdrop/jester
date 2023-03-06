package io.github.jester.resources.spring.kubernetes;

import io.github.jester.api.Spring;
import io.github.jester.api.extensions.SpringManagedResourceBinding;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesSpringManagedResourceBinding implements SpringManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Spring metadata) {
        return new ContainerRegistrySpringKubernetesManagedResource(metadata.location(), metadata.forceBuild(),
                metadata.buildCommands());
    }
}
