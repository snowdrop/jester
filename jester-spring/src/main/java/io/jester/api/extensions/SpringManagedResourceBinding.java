package io.jester.api.extensions;

import io.jester.api.Spring;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;

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
