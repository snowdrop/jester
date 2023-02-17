package io.jester.resources.quarkus.openshift;

import io.jester.api.Quarkus;
import io.jester.api.extensions.QuarkusManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.core.extensions.OpenShiftExtensionBootstrap;

public class OpenShiftQuarkusManagedResourceBinding implements QuarkusManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return OpenShiftExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(Quarkus metadata) {
        return new ContainerRegistryProdModeBootstrapQuarkusOpenShiftManagedResource(metadata.location(),
                metadata.classes(), metadata.dependencies(), metadata.forceBuild(), metadata.version());
    }
}
