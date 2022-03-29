package io.jcloud.api.extensions;

import io.jcloud.api.GitRemoteProject;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ScenarioContext;

public interface GitRemoteProjectManagedResourceBinding {
    /**
     * @param context
     *
     * @return if the current managed resource applies for the current context.
     */
    boolean appliesFor(ScenarioContext context);

    /**
     * Init and return the managed resource for the current context.
     *
     * @param metadata
     *
     * @return
     */
    ManagedResource init(GitRemoteProject metadata);
}
