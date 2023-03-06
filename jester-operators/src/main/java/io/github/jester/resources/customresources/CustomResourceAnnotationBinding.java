package io.github.jester.resources.customresources;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.github.jester.api.CustomResource;
import io.github.jester.api.Service;
import io.github.jester.api.extensions.AnnotationBinding;
import io.github.jester.api.extensions.CustomResourceManagedResourceBinding;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;

public class CustomResourceAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<CustomResourceManagedResourceBinding> bindings = ServiceLoader
            .load(CustomResourceManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, CustomResource.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
        CustomResource metadata = findAnnotation(annotations, CustomResource.class).get();

        for (CustomResourceManagedResourceBinding binding : bindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata.resource(), metadata.type());
            }
        }

        throw new UnsupportedOperationException("Unsupported environment for @CustomResource service");
    }
}
