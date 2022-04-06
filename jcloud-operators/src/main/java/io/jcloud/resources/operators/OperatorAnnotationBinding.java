package io.jcloud.resources.operators;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jcloud.api.Operator;
import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.api.extensions.OperatorManagedResourceBinding;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;

public class OperatorAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<OperatorManagedResourceBinding> bindings = ServiceLoader
            .load(OperatorManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, Operator.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JCloudContext context, Annotation... annotations) {
        Operator metadata = findAnnotation(annotations, Operator.class).get();

        for (OperatorManagedResourceBinding binding : bindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata);
            }
        }

        throw new UnsupportedOperationException("Unsupported environment for @Operator service");
    }
}
