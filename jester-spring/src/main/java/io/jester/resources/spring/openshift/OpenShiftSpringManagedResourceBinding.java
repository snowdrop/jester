package io.jester.resources.spring.openshift;

import io.jester.api.Spring;
import io.jester.api.extensions.SpringManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.OpenShiftExtensionBootstrap;

public class OpenShiftSpringManagedResourceBinding implements SpringManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return OpenShiftExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Spring metadata) {
        return new ContainerRegistrySpringOpenShiftManagedResource(metadata.location(), metadata.forceBuild(),
                metadata.buildCommands());
    }
}
