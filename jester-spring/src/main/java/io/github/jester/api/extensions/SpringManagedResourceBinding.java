package io.github.jester.api.extensions;

import io.github.jester.api.Spring;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;

public interface SpringManagedResourceBinding {
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
    ManagedResource init(Spring metadata);
}
