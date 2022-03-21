package io.jcloud.resources.localsource.kubernetes;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.LocalSource;
import io.jcloud.api.RunOnKubernetes;
import io.jcloud.api.extensions.LocalSourceManagedResourceBinding;
import io.jcloud.core.ManagedResource;

public class KubernetesLocalSourceManagedResourceBinding implements LocalSourceManagedResourceBinding {
    @Override
    public boolean appliesFor(ExtensionContext context) {
        return context.getRequiredTestClass().isAnnotationPresent(RunOnKubernetes.class);
    }

    @Override
    public ManagedResource init(LocalSource metadata) {
        return new KubernetesLocalSourceManagedResource(metadata.location(), metadata.buildCommands(),
                metadata.dockerfile(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }
}
