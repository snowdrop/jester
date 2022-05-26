package io.jester.api.extensions;

import io.fabric8.kubernetes.client.CustomResource;
import io.jester.api.model.CustomResourceSpec;
import io.jester.api.model.CustomResourceStatus;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;

public interface CustomResourceManagedResourceBinding {
    /**
     * @param context
     *
     * @return if the current managed resource applies for the current context.
     */
    boolean appliesFor(JesterContext context);

    /**
     * Init and return the managed resource for the current context.
     *
     * @return
     */
    ManagedResource init(String resource,
            Class<? extends CustomResource<CustomResourceSpec, CustomResourceStatus>> type);
}
