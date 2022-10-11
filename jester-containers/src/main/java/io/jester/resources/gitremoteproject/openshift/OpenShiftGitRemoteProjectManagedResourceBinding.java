package io.jester.resources.gitremoteproject.openshift;

import io.jester.api.GitRemoteProject;
import io.jester.api.extensions.GitRemoteProjectManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.OpenShiftExtensionBootstrap;

public class OpenShiftGitRemoteProjectManagedResourceBinding implements GitRemoteProjectManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return OpenShiftExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(GitRemoteProject metadata) {
        return new OpenShiftGitRemoteProjectManagedResource(metadata.repo(), metadata.branch(), metadata.contextDir(),
                metadata.buildCommands(), metadata.dockerfile(), metadata.expectedLog(), metadata.command(),
                metadata.ports());
    }
}
