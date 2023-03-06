package io.github.jester.resources.quarkus.openshift;

import io.github.jester.api.Quarkus;
import io.github.jester.api.extensions.QuarkusManagedResourceBinding;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.extensions.OpenShiftExtensionBootstrap;

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
