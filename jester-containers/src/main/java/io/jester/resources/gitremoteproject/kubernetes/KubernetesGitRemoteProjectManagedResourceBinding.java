package io.jester.resources.gitremoteproject.kubernetes;

import io.jester.api.GitRemoteProject;
import io.jester.api.extensions.GitRemoteProjectManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesGitRemoteProjectManagedResourceBinding implements GitRemoteProjectManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(GitRemoteProject metadata) {
        return new KubernetesGitRemoteProjectManagedResource(metadata.repo(), metadata.branch(), metadata.contextDir(),
                metadata.buildCommands(), metadata.dockerfile(), metadata.expectedLog(), metadata.command(),
                metadata.ports());
    }
}
