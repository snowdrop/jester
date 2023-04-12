package io.github.snowdrop.jester.resources.gitremoteproject.openshift;

import io.github.snowdrop.jester.api.GitRemoteProject;
import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.extensions.GitRemoteProjectManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.OpenShiftExtensionBootstrap;

public class OpenShiftGitRemoteProjectManagedResourceBinding implements GitRemoteProjectManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return OpenShiftExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(JesterContext context, Service service, GitRemoteProject metadata) {
        return new OpenShiftGitRemoteProjectManagedResource(metadata.repo(), metadata.branch(), metadata.contextDir(),
                metadata.buildCommands(), metadata.dockerfile(), metadata.expectedLog(), metadata.command(),
                metadata.ports());
    }
}
