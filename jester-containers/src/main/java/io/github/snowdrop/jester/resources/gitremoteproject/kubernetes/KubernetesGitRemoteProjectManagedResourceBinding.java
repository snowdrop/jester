package io.github.snowdrop.jester.resources.gitremoteproject.kubernetes;

import io.github.snowdrop.jester.api.GitRemoteProject;
import io.github.snowdrop.jester.api.extensions.GitRemoteProjectManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.KubernetesExtensionBootstrap;

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
