package io.github.snowdrop.jester.resources.containers.openshift;

import io.github.snowdrop.jester.api.extensions.ContainerManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.OpenShiftExtensionBootstrap;

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
