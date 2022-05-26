package io.jester.api.extensions;

import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;

public interface ContainerManagedResourceBinding {
    /**
     * @param context
     *
     * @return if the current managed resource applies for the current context.
     */
    boolean appliesFor(JesterContext context);

    /**
     * Init and return the managed resource for the current context.
     */
    ManagedResource init(String image, String expectedLog, String[] command, int[] ports);
}
