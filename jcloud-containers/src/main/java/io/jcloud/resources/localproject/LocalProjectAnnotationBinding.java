package io.jcloud.resources.localproject;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.LocalProject;
import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.api.extensions.LocalProjectManagedResourceBinding;
import io.jcloud.core.ManagedResource;
import io.jcloud.resources.localproject.local.DockerLocalProjectManagedResource;

public class LocalProjectAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<LocalProjectManagedResourceBinding> bindings = ServiceLoader
            .load(LocalProjectManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, LocalProject.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(ExtensionContext context, Annotation... annotations) {
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
