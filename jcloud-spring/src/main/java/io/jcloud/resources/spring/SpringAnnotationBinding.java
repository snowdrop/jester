package io.jcloud.resources.spring;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jcloud.api.Spring;
import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.api.extensions.SpringManagedResourceBinding;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;
import io.jcloud.resources.spring.local.LocalBootstrapSpringManagedResource;

public class SpringAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<SpringManagedResourceBinding> customBindings = ServiceLoader
            .load(SpringManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, Spring.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JCloudContext context, Annotation... annotations) {
        Spring metadata = findAnnotation(annotations, Spring.class).get();

        for (SpringManagedResourceBinding binding : customBindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new LocalBootstrapSpringManagedResource(metadata.location(), metadata.forceBuild(),
                metadata.buildCommands());
    }

}
