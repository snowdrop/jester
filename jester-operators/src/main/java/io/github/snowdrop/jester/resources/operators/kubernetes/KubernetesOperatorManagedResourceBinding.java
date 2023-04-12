package io.github.snowdrop.jester.resources.operators.kubernetes;

import io.github.snowdrop.jester.api.Operator;
import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.extensions.OperatorManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesOperatorManagedResourceBinding implements OperatorManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return KubernetesExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(JesterContext context, Service service, Operator metadata) {
        return new KubernetesOperatorManagedResource(metadata.subscription(), metadata.channel(), metadata.source(),
                metadata.sourceNamespace());
    }
}
