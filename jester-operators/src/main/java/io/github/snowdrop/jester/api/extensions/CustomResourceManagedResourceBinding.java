package io.github.snowdrop.jester.api.extensions;

import io.fabric8.kubernetes.client.CustomResource;
import io.github.snowdrop.jester.api.model.CustomResourceSpec;
import io.github.snowdrop.jester.api.model.CustomResourceStatus;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;

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
