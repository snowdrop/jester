package io.jcloud.resources.containers;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jcloud.api.Container;
import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.api.extensions.ContainerManagedResourceBinding;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;
import io.jcloud.resources.containers.local.DockerContainerManagedResource;

public class ContainerAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<ContainerManagedResourceBinding> containerBindings = ServiceLoader
            .load(ContainerManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, Container.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JCloudContext context, Annotation... annotations) {
        Container metadata = findAnnotation(annotations, Container.class).get();

        for (ContainerManagedResourceBinding binding : containerBindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new DockerContainerManagedResource(metadata.image(), metadata.expectedLog(), metadata.command(),
                metadata.ports());
    }

}
