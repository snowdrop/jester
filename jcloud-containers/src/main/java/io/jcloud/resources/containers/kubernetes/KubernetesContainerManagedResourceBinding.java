package io.jcloud.resources.containers.kubernetes;

import io.jcloud.api.Container;
import io.jcloud.api.extensions.ContainerManagedResourceBinding;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesContainerManagedResourceBinding implements ContainerManagedResourceBinding {
    @Override
    public boolean appliesFor(JCloudContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Container metadata) {
        return new KubernetesContainerManagedResource(metadata.image(), metadata.expectedLog(), metadata.command(),
                metadata.ports());
    }
}
