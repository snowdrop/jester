package io.jcloud.resources.spring.kubernetes;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.RunOnKubernetes;
import io.jcloud.api.Spring;
import io.jcloud.api.extensions.SpringManagedResourceBinding;
import io.jcloud.core.ManagedResource;

public class KubernetesQuarkusManagedResourceBinding implements SpringManagedResourceBinding {
    @Override
    public boolean appliesFor(ExtensionContext context) {
        return context.getRequiredTestClass().isAnnotationPresent(RunOnKubernetes.class);
    }

    @Override
    public ManagedResource init(Spring metadata) {
        return new ContainerRegistrySpringManagedResource();
    }
}
