package io.github.snowdrop.jester.resources.quarkus;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.extensions.AnnotationBinding;
import io.github.snowdrop.jester.api.extensions.QuarkusManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.resources.quarkus.local.ProdModeBootstrapQuarkusJavaProcessManagedResource;

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
                return binding.init(context, service, metadata);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new ProdModeBootstrapQuarkusJavaProcessManagedResource(metadata.location(), metadata.classes(),
                metadata.dependencies(), metadata.forceBuild(), metadata.version());
    }

}
