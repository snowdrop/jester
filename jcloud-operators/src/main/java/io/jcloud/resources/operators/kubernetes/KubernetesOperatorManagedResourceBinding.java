package io.jcloud.resources.operators.kubernetes;

import io.jcloud.api.Operator;
import io.jcloud.api.extensions.OperatorManagedResourceBinding;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesOperatorManagedResourceBinding implements OperatorManagedResourceBinding {
    @Override
    public boolean appliesFor(JCloudContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Operator metadata) {
        return new KubernetesOperatorManagedResource(metadata.name(), metadata.channel(), metadata.source(),
                metadata.sourceNamespace());
    }
}
