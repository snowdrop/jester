package io.jcloud.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ReflectionUtils {

    private ReflectionUtils() {

    }

    public static <T> T getStaticFieldValue(Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Can't resolve field value. Fields need to be in static. Problematic field: " + field.getName(), e);
        }
    }

    public static void setStaticFieldValue(Field field, Object value) {
        field.setAccessible(true);
        if (Modifier.isStatic(field.getModifiers())) {
            try {
                field.set(null, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        "Couldn't set value. Fields can only be injected into static instances. Problematic field: "
                                + field.getName(),
                        e);
            }
        } else {
            throw new RuntimeException(
                    "Fields can only be injected into static instances. Problematic field: " + field.getName());
        }
    }

    public static List<Annotation> findAllAnnotations(Class<?> clazz) {
        if (clazz == Object.class) {
            return Collections.emptyList();
        }

        List<Annotation> annotations = new ArrayList<>();
        annotations.addAll(findAllAnnotations(clazz.getSuperclass()));
        annotations.addAll(Arrays.asList(clazz.getAnnotations()));
        return annotations;
    }

    public static List<Field> findAllFields(Class<?> clazz) {
        if (clazz == Object.class) {
            return Collections.emptyList();
        }

        List<Field> fields = new ArrayList<>();
        fields.addAll(findAllFields(clazz.getSuperclass()));
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        return fields;
    }

    public static <T> T createInstance(Class<T> clazz, Object... args) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == args.length) {
                try {
                    return (T) constructor.newInstance(args);
                } catch (Exception ex) {
                    throw new RuntimeException("Constructor failed to be called.", ex);
                }
            }
        }

        throw new RuntimeException("Constructor not found for " + clazz);
    }

    public static Object invokeMethod(Object instance, String methodName, Object... args) {
        for (Method method : instance.getClass().getMethods()) {
            if (methodName.equals(method.getName())) {
                return org.junit.platform.commons.util.ReflectionUtils.invokeMethod(method, instance, args);
            }
        }

        throw new RuntimeException("Method " + methodName + " not found in " + instance.getClass());
    }
}
