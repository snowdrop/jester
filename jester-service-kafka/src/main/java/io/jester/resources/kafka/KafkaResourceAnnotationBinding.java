package io.jester.resources.kafka;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jester.api.KafkaResource;
import io.jester.api.Service;
import io.jester.api.extensions.AnnotationBinding;
import io.jester.api.extensions.CustomResourceManagedResourceBinding;
import io.jester.api.model.KafkaInstanceCustomResource;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;

public class KafkaResourceAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<CustomResourceManagedResourceBinding> bindings = ServiceLoader
            .load(CustomResourceManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, KafkaResource.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
        KafkaResource metadata = findAnnotation(annotations, KafkaResource.class).get();

        for (CustomResourceManagedResourceBinding binding : bindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata.resource(), KafkaInstanceCustomResource.class);
            }
        }

        throw new UnsupportedOperationException("Unsupported environment for @Operator service");
    }
}
