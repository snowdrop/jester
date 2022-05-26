package io.jester.resources.customresources.kubernetes;

import io.fabric8.kubernetes.client.CustomResource;
import io.jester.api.extensions.CustomResourceManagedResourceBinding;
import io.jester.api.model.CustomResourceSpec;
import io.jester.api.model.CustomResourceStatus;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesCustomResourceManagedResourceBinding implements CustomResourceManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(String resource,
            Class<? extends CustomResource<CustomResourceSpec, CustomResourceStatus>> type) {
        return new KubernetesCustomResourceManagedResource(resource, type);
    }
}
