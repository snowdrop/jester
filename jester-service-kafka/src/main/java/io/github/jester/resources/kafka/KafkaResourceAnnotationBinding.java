package io.github.jester.resources.kafka;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.github.jester.api.KafkaResource;
import io.github.jester.api.Service;
import io.github.jester.api.extensions.AnnotationBinding;
import io.github.jester.api.extensions.CustomResourceManagedResourceBinding;
import io.github.jester.api.model.KafkaInstanceCustomResource;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;

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
