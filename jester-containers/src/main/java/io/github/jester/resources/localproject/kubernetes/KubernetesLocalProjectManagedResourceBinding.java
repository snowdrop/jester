package io.github.jester.resources.localproject.kubernetes;

import io.github.jester.api.LocalProject;
import io.github.jester.api.extensions.LocalProjectManagedResourceBinding;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.extensions.KubernetesExtensionBootstrap;

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
