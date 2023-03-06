package io.github.jester.core;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ParameterContext;

public final class DependencyContext {

    private final String name;
    private final Class<?> paramType;
    private final Annotation[] annotations;

    public DependencyContext(ParameterContext parameterContext) {
        this(parameterContext.getParameter().getName(), parameterContext.getParameter().getType(),
                parameterContext.getParameter().getAnnotations());
    }

    public DependencyContext(String name, Class<?> paramType, Annotation[] annotations) {
        this.name = name;
        this.paramType = paramType;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return paramType;
    }

    public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationType) {
        return Stream.of(annotations).filter(ann -> ann.annotationType() == annotationType).map(ann -> (T) ann)
                .findFirst();
    }
}
