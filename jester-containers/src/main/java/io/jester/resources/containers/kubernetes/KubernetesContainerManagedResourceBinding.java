package io.jester.resources.containers.kubernetes;

import io.jester.api.extensions.ContainerManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesContainerManagedResourceBinding implements ContainerManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(String image, String expectedLog, String[] command, int[] ports) {
        return new KubernetesContainerManagedResource(image, expectedLog, command, ports);
    }
}
