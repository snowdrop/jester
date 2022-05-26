package io.jester.resources.localproject.kubernetes;

import io.jester.api.LocalProject;
import io.jester.api.extensions.LocalProjectManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesLocalProjectManagedResourceBinding implements LocalProjectManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(LocalProject metadata) {
        return new KubernetesLocalProjectManagedResource(metadata.location(), metadata.buildCommands(),
                metadata.dockerfile(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }
}
