package io.jcloud.resources.localproject.kubernetes;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.LocalProject;
import io.jcloud.api.RunOnKubernetes;
import io.jcloud.api.extensions.LocalProjectManagedResourceBinding;
import io.jcloud.core.ManagedResource;

public class KubernetesLocalProjectManagedResourceBinding implements LocalProjectManagedResourceBinding {
    @Override
    public boolean appliesFor(ExtensionContext context) {
        return context.getRequiredTestClass().isAnnotationPresent(RunOnKubernetes.class);
    }

    @Override
    public ManagedResource init(LocalProject metadata) {
        return new KubernetesLocalProjectManagedResource(metadata.location(), metadata.buildCommands(),
                metadata.dockerfile(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }
}
