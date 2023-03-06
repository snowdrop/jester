package io.github.snowdrop.jester.api.extensions;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.snowdrop.jester.api.RestService;
import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;

public interface AnnotationBinding {
    boolean isFor(Annotation... annotations);

    ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations)
            throws Exception;

    default <T extends Annotation> Optional<T> findAnnotation(Annotation[] annotations, Class<T> clazz) {
        return Stream.of(annotations).filter(clazz::isInstance).map(a -> (T) a).findFirst();
    }

    /**
     * Return the default service implementation for the current annotation. Used for annotations that are used at class
     * level.
     */
    default Service getDefaultServiceImplementation() {
        return new RestService();
    }

    /**
     * Return the default service name for the current annotation. Used for annotations that are used at class level.
     */
    default String getDefaultName(Annotation annotation) {
        return annotation.annotationType().getSimpleName().toLowerCase();
    }
}
