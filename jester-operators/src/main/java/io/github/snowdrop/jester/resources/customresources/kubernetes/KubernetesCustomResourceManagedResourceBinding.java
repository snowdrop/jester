package io.github.snowdrop.jester.resources.customresources.kubernetes;

import io.fabric8.kubernetes.client.CustomResource;
import io.github.snowdrop.jester.api.extensions.CustomResourceManagedResourceBinding;
import io.github.snowdrop.jester.api.model.CustomResourceSpec;
import io.github.snowdrop.jester.api.model.CustomResourceStatus;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.KubernetesExtensionBootstrap;

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
