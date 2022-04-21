package io.jcloud.resources.kafka;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jcloud.api.KafkaResource;
import io.jcloud.api.Service;
import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.api.extensions.CustomResourceManagedResourceBinding;
import io.jcloud.api.model.KafkaInstanceCustomResource;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;

public class KafkaResourceAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<CustomResourceManagedResourceBinding> bindings = ServiceLoader
            .load(CustomResourceManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, KafkaResource.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JCloudContext context, Service service, Annotation... annotations) {
        KafkaResource metadata = findAnnotation(annotations, KafkaResource.class).get();

        for (CustomResourceManagedResourceBinding binding : bindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata.resource(), KafkaInstanceCustomResource.class);
            }
        }

        throw new UnsupportedOperationException("Unsupported environment for @Operator service");
    }
}
