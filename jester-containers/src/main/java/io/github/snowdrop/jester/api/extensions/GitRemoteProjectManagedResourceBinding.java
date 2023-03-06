package io.github.snowdrop.jester.api.extensions;

import io.github.snowdrop.jester.api.GitRemoteProject;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;

public interface GitRemoteProjectManagedResourceBinding {
    /**
     * @param context
     *
     * @return if the current managed resource applies for the current context.
     */
    boolean appliesFor(JesterContext context);

    /**
     * Init and return the managed resource for the current context.
     *
     * @param metadata
     *
     * @return
     */
    ManagedResource init(GitRemoteProject metadata);
}
