package io.github.snowdrop.jester.resources.containers;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.github.snowdrop.jester.api.Container;
import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.extensions.AnnotationBinding;
import io.github.snowdrop.jester.api.extensions.ContainerManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.resources.containers.local.DockerContainerManagedResource;

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

        return doInit(context, service, metadata.image(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }

    protected ManagedResource doInit(JesterContext context, Service service, String image, String expectedLog,
            String[] command, int[] ports) {
        for (ContainerManagedResourceBinding binding : containerBindings) {
            if (binding.appliesFor(context)) {
                return binding.init(context, service, image, expectedLog, command, ports);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new DockerContainerManagedResource(image, expectedLog, command, ports);
    }

}
