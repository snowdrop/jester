package io.jcloud.api.extensions;

import io.jcloud.api.Quarkus;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ScenarioContext;

public interface QuarkusManagedResourceBinding {
    /**
     * @param context
     * @return if the current managed resource applies for the current context.
     */
    boolean appliesFor(ScenarioContext context);

    /**
     * Init and return the managed resource for the current context.
     *
     * @param metadata
     * @return
     */
    ManagedResource init(Quarkus metadata);
}
