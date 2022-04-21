package io.jcloud.api.extensions;

import io.fabric8.kubernetes.client.CustomResource;
import io.jcloud.api.model.CustomResourceSpec;
import io.jcloud.api.model.CustomResourceStatus;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;

public interface CustomResourceManagedResourceBinding {
    /**
     * @param context
     *
     * @return if the current managed resource applies for the current context.
     */
    boolean appliesFor(JCloudContext context);

    /**
     * Init and return the managed resource for the current context.
     *
     * @return
     */
    ManagedResource init(String resource,
            Class<? extends CustomResource<CustomResourceSpec, CustomResourceStatus>> type);
}
