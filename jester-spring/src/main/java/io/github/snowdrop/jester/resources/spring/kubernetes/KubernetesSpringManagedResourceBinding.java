package io.github.snowdrop.jester.resources.spring.kubernetes;

import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.Spring;
import io.github.snowdrop.jester.api.extensions.SpringManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesSpringManagedResourceBinding implements SpringManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(JesterContext context, Service service, Spring metadata) {
        return new ContainerRegistrySpringKubernetesManagedResource(metadata.location(), metadata.forceBuild(),
                metadata.buildCommands());
    }
}
