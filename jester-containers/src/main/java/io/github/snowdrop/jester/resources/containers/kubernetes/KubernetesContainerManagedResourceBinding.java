package io.github.snowdrop.jester.resources.containers.kubernetes;

import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.extensions.ContainerManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesContainerManagedResourceBinding implements ContainerManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(JesterContext context, Service service, String image, String expectedLog,
            String[] command, int[] ports) {
        return new KubernetesContainerManagedResource(image, expectedLog, command, ports);
    }
}
