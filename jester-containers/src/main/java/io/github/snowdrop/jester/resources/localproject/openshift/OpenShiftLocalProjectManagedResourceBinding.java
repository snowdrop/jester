package io.github.snowdrop.jester.resources.localproject.openshift;

import io.github.snowdrop.jester.api.LocalProject;
import io.github.snowdrop.jester.api.extensions.LocalProjectManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.OpenShiftExtensionBootstrap;

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
