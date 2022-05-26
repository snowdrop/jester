package io.jester.resources.operators.kubernetes;

import io.jester.api.Operator;
import io.jester.api.extensions.OperatorManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesOperatorManagedResourceBinding implements OperatorManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Operator metadata) {
        return new KubernetesOperatorManagedResource(metadata.subscription(), metadata.channel(), metadata.source(),
                metadata.sourceNamespace());
    }
}
