package io.github.jester.resources.customresources.kubernetes;

import io.fabric8.kubernetes.client.CustomResource;
import io.github.jester.api.extensions.CustomResourceManagedResourceBinding;
import io.github.jester.api.model.CustomResourceSpec;
import io.github.jester.api.model.CustomResourceStatus;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.extensions.KubernetesExtensionBootstrap;

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
