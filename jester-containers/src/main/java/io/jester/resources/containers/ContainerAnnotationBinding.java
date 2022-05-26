package io.jester.resources.containers;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jester.api.Container;
import io.jester.api.Service;
import io.jester.api.extensions.AnnotationBinding;
import io.jester.api.extensions.ContainerManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.resources.containers.local.DockerContainerManagedResource;

public class ContainerAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<ContainerManagedResourceBinding> containerBindings = ServiceLoader
            .load(ContainerManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, Container.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
        Container metadata = findAnnotation(annotations, Container.class).get();

        return doInit(context, metadata.image(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }

    protected ManagedResource doInit(JesterContext context, String image, String expectedLog, String[] command,
            int[] ports) {
        for (ContainerManagedResourceBinding binding : containerBindings) {
            if (binding.appliesFor(context)) {
                return binding.init(image, expectedLog, command, ports);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new DockerContainerManagedResource(image, expectedLog, command, ports);
    }

}
