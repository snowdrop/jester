package io.github.jester.resources.spring.openshift;

import io.github.jester.api.Spring;
import io.github.jester.api.extensions.SpringManagedResourceBinding;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.extensions.OpenShiftExtensionBootstrap;

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
