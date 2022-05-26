package io.jester.resources.spring;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jester.api.Service;
import io.jester.api.Spring;
import io.jester.api.extensions.AnnotationBinding;
import io.jester.api.extensions.SpringManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.resources.spring.local.LocalBootstrapSpringManagedResourceJava;

public class SpringAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<SpringManagedResourceBinding> customBindings = ServiceLoader
            .load(SpringManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, Spring.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
        Spring metadata = findAnnotation(annotations, Spring.class).get();

        for (SpringManagedResourceBinding binding : customBindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new LocalBootstrapSpringManagedResourceJava(metadata.location(), metadata.forceBuild(),
                metadata.buildCommands());
    }

}
