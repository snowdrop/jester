package io.jcloud.resources.spring.kubernetes;

import io.jcloud.api.Spring;
import io.jcloud.api.extensions.SpringManagedResourceBinding;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ScenarioContext;
import io.jcloud.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesSpringManagedResourceBinding implements SpringManagedResourceBinding {
    @Override
    public boolean appliesFor(ScenarioContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Spring metadata) {
        return new ContainerRegistrySpringManagedResource(metadata.location(), metadata.forceBuild(),
                metadata.buildCommands());
    }
}
