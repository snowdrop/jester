package io.github.jester.resources.localproject;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.github.jester.api.LocalProject;
import io.github.jester.api.Service;
import io.github.jester.api.extensions.AnnotationBinding;
import io.github.jester.api.extensions.LocalProjectManagedResourceBinding;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.resources.localproject.local.DockerLocalProjectManagedResource;

public class LocalProjectAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<LocalProjectManagedResourceBinding> bindings = ServiceLoader
            .load(LocalProjectManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, LocalProject.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
        LocalProject metadata = findAnnotation(annotations, LocalProject.class).get();

        for (LocalProjectManagedResourceBinding binding : bindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new DockerLocalProjectManagedResource(metadata.location(), metadata.buildCommands(),
                metadata.dockerfile(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }

}
