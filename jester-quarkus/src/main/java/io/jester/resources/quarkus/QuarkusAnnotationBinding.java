package io.jester.resources.quarkus;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jester.api.Quarkus;
import io.jester.api.Service;
import io.jester.api.extensions.AnnotationBinding;
import io.jester.api.extensions.QuarkusManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.resources.quarkus.local.ProdModeBootstrapQuarkusManagedResourceJava;

public class QuarkusAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<QuarkusManagedResourceBinding> customBindings = ServiceLoader
            .load(QuarkusManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, Quarkus.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
        Quarkus metadata = findAnnotation(annotations, Quarkus.class).get();

        for (QuarkusManagedResourceBinding binding : customBindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new ProdModeBootstrapQuarkusManagedResourceJava(metadata.location(), metadata.buildCommands(),
                metadata.classes(), metadata.dependencies(), metadata.forceBuild());
    }

}
