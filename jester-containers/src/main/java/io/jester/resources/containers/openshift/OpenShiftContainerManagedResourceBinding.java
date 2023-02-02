package io.jester.resources.containers.openshift;

import io.jester.api.extensions.ContainerManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.OpenShiftExtensionBootstrap;

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
