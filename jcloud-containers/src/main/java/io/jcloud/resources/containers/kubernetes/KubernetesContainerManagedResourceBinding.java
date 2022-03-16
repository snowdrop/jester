package io.jcloud.resources.containers.kubernetes;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.Container;
import io.jcloud.api.RunOnKubernetes;
import io.jcloud.api.extensions.ContainerManagedResourceBinding;
import io.jcloud.core.ManagedResource;

public class KubernetesContainerManagedResourceBinding implements ContainerManagedResourceBinding {
    @Override
    public boolean appliesFor(ExtensionContext context) {
        return context.getRequiredTestClass().isAnnotationPresent(RunOnKubernetes.class);
    }

    @Override
    public ManagedResource init(Container metadata) {
        return new KubernetesContainerManagedResource(metadata.image(), metadata.expectedLog(), metadata.command(),
                metadata.ports());
    }
}
