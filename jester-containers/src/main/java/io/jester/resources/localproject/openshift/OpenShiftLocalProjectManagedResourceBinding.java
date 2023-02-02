package io.jester.resources.localproject.openshift;

import io.jester.api.LocalProject;
import io.jester.api.extensions.LocalProjectManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.OpenShiftExtensionBootstrap;

public class OpenShiftLocalProjectManagedResourceBinding implements LocalProjectManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return OpenShiftExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(LocalProject metadata) {
        return new OpenShiftLocalProjectManagedResource(metadata.location(), metadata.buildCommands(),
                metadata.dockerfile(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }
}
