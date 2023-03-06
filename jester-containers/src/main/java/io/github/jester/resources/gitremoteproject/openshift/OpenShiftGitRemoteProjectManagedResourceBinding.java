package io.github.jester.resources.gitremoteproject.openshift;

import io.github.jester.api.GitRemoteProject;
import io.github.jester.api.extensions.GitRemoteProjectManagedResourceBinding;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.extensions.OpenShiftExtensionBootstrap;

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
