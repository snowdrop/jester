package io.jcloud.resources.customresources;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jcloud.api.CustomResource;
import io.jcloud.api.Service;
import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.api.extensions.CustomResourceManagedResourceBinding;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;

public class CustomResourceAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<CustomResourceManagedResourceBinding> bindings = ServiceLoader
            .load(CustomResourceManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, CustomResource.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JCloudContext context, Service service, Annotation... annotations) {
        CustomResource metadata = findAnnotation(annotations, CustomResource.class).get();

        for (CustomResourceManagedResourceBinding binding : bindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata.resource(), metadata.type());
            }
        }

        throw new UnsupportedOperationException("Unsupported environment for @CustomResource service");
    }
}
