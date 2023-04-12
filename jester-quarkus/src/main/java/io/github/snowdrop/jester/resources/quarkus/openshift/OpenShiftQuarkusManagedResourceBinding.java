package io.github.snowdrop.jester.resources.quarkus.openshift;

import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.extensions.QuarkusManagedResourceBinding;
import io.github.snowdrop.jester.configuration.DeploymentMethod;
import io.github.snowdrop.jester.configuration.QuarkusServiceConfiguration;
import io.github.snowdrop.jester.configuration.QuarkusServiceConfigurationBuilder;
import io.github.snowdrop.jester.configuration.ServiceConfigurationLoader;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.extensions.OpenShiftExtensionBootstrap;
import io.github.snowdrop.jester.utils.QuarkusUtils;

public class OpenShiftQuarkusManagedResourceBinding implements QuarkusManagedResourceBinding {
    @Override
    public boolean appliesFor(JesterContext context) {
        return OpenShiftExtensionBootstrap.isEnabled(context);
    }

    @Override
    public ManagedResource init(JesterContext context, Service service, Quarkus metadata) {
        QuarkusServiceConfiguration config = ServiceConfigurationLoader.load(service.getName(), context,
                new QuarkusServiceConfigurationBuilder());
        if (config.getDeploymentMethod() == DeploymentMethod.USING_EXTENSION
                || (config.getDeploymentMethod() == DeploymentMethod.AUTO
                        && QuarkusUtils.isKubernetesExtensionLoaded())) {
            return new UsingExtensionQuarkusOpenShiftManagedResource(metadata.location());
        }

        return new ContainerRegistryProdModeBootstrapQuarkusOpenShiftManagedResource(metadata.location(),
                metadata.classes(), metadata.dependencies(), metadata.forceBuild(), metadata.version());
    }
}
