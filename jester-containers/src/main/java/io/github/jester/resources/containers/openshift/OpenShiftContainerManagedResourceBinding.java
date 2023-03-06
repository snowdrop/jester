package io.github.jester.resources.containers.openshift;

import io.github.jester.api.extensions.ContainerManagedResourceBinding;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.extensions.OpenShiftExtensionBootstrap;

public class OpenShiftContainerManagedResourceBinding implements ContainerManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return OpenShiftExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(String image, String expectedLog, String[] command, int[] ports) {
        return new OpenShiftContainerManagedResource(image, expectedLog, command, ports);
    }
}
