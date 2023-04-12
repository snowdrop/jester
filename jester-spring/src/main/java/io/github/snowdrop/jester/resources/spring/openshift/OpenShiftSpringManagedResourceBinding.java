package io.github.snowdrop.jester.resources.spring.openshift;

import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.Spring;
import io.github.snowdrop.jester.api.extensions.SpringManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.OpenShiftExtensionBootstrap;

public class OpenShiftSpringManagedResourceBinding implements SpringManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return OpenShiftExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(JesterContext context, Service service, Spring metadata) {
        return new ContainerRegistrySpringOpenShiftManagedResource(metadata.location(), metadata.forceBuild(),
                metadata.buildCommands());
    }
}
