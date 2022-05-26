package io.jester.resources.customresources;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jester.api.CustomResource;
import io.jester.api.Service;
import io.jester.api.extensions.AnnotationBinding;
import io.jester.api.extensions.CustomResourceManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;

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
