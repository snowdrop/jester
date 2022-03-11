package io.jcloud.resources.containers;

import java.lang.reflect.Field;
import java.util.ServiceLoader;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.Container;
import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.api.extensions.ContainerManagedResourceBinding;
import io.jcloud.core.ManagedResource;
import io.jcloud.resources.containers.local.DockerContainerManagedResource;

public class ContainerAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<ContainerManagedResourceBinding> containerBindings = ServiceLoader
            .load(ContainerManagedResourceBinding.class);

    @Override
    public boolean isFor(Field field) {
        return field.isAnnotationPresent(Container.class);
    }

    @Override
    public ManagedResource getManagedResource(ExtensionContext context, Field field) {
        Container metadata = field.getAnnotation(Container.class);

        for (ContainerManagedResourceBinding binding : containerBindings) {
            if (binding.appliesFor(context)) {
                return binding.init(field);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new DockerContainerManagedResource(metadata.image(), metadata.expectedLog(), metadata.command(),
                metadata.ports());
    }

}
