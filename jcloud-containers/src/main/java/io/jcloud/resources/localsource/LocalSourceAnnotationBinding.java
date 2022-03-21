package io.jcloud.resources.localsource;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.LocalSource;
import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.api.extensions.LocalSourceManagedResourceBinding;
import io.jcloud.core.ManagedResource;
import io.jcloud.resources.localsource.local.DockerLocalSourceManagedResource;

public class LocalSourceAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<LocalSourceManagedResourceBinding> bindings = ServiceLoader
            .load(LocalSourceManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, LocalSource.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(ExtensionContext context, Annotation... annotations) {
        LocalSource metadata = findAnnotation(annotations, LocalSource.class).get();

        for (LocalSourceManagedResourceBinding binding : bindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new DockerLocalSourceManagedResource(metadata.location(), metadata.buildCommands(),
                metadata.dockerfile(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }

}
