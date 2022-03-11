package io.jcloud.api.extensions;

import java.lang.reflect.Field;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.core.ManagedResource;

public interface AnnotationBinding {
    boolean isFor(Field field);

    ManagedResource getManagedResource(ExtensionContext context, Field field) throws Exception;
}
