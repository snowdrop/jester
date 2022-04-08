package io.jcloud.api.extensions;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;

import io.jcloud.api.Service;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;

public interface AnnotationBinding {
    boolean isFor(Annotation... annotations);

    ManagedResource getManagedResource(JCloudContext context, Service service, Annotation... annotations)
            throws Exception;

    default <T extends Annotation> Optional<T> findAnnotation(Annotation[] annotations, Class<T> clazz) {
        return Stream.of(annotations).filter(clazz::isInstance).map(a -> (T) a).findFirst();
    }
}
