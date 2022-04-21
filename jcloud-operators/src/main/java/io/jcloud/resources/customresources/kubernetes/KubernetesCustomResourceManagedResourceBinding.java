package io.jcloud.resources.customresources.kubernetes;

import io.fabric8.kubernetes.client.CustomResource;
import io.jcloud.api.extensions.CustomResourceManagedResourceBinding;
import io.jcloud.api.model.CustomResourceSpec;
import io.jcloud.api.model.CustomResourceStatus;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesCustomResourceManagedResourceBinding implements CustomResourceManagedResourceBinding {
    @Override
    public boolean appliesFor(JCloudContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(String resource,
            Class<? extends CustomResource<CustomResourceSpec, CustomResourceStatus>> type) {
        return new KubernetesCustomResourceManagedResource(resource, type);
    }
}
