package io.github.snowdrop.jester.resources.spring;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.Spring;
import io.github.snowdrop.jester.api.extensions.AnnotationBinding;
import io.github.snowdrop.jester.api.extensions.SpringManagedResourceBinding;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.resources.spring.local.LocalBootstrapSpringJavaProcessManagedResource;

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
                return binding.init(context, service, metadata);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new LocalBootstrapSpringJavaProcessManagedResource(metadata.location(), metadata.forceBuild(),
                metadata.buildCommands());
    }

}
