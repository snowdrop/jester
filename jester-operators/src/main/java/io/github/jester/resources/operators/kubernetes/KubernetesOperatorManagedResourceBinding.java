package io.github.jester.resources.operators.kubernetes;

import io.github.jester.api.Operator;
import io.github.jester.api.extensions.OperatorManagedResourceBinding;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.extensions.KubernetesExtensionBootstrap;

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
