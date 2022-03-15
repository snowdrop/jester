package io.jcloud.resources.containers.kubernetes;

import java.lang.reflect.Field;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.Container;
import io.jcloud.api.KubernetesScenario;
import io.jcloud.api.extensions.ContainerManagedResourceBinding;
import io.jcloud.core.ManagedResource;

public class KubernetesContainerManagedResourceBinding implements ContainerManagedResourceBinding {
    @Override
    public boolean appliesFor(ExtensionContext context) {
        return context.getRequiredTestClass().isAnnotationPresent(KubernetesScenario.class);
    }

    @Override
    public ManagedResource init(Field field) {
        Container metadata = field.getAnnotation(Container.class);
        return new KubernetesContainerManagedResource(metadata.image(), metadata.expectedLog(), metadata.command(),
                metadata.ports());
    }
}
