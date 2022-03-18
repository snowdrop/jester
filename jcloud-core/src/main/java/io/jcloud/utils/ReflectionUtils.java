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
import java.util.Optional;

public final class ReflectionUtils {

    private ReflectionUtils() {

    }

    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public static boolean isInstance(Field field) {
        return !isStatic(field);
    }

    public static <T> T getFieldValue(Optional<Object> testInstance, Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(testInstance.orElse(null));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't resolve field value. Problematic field: " + field.getName(), e);
        }
    }

    public static void setFieldValue(Optional<Object> testInstance, Field field, Object value) {
        field.setAccessible(true);
        try {
            field.set(testInstance.orElse(null), value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Couldn't set value. Problematic field: " + field.getName(), e);
        }
    }

    public static List<Annotation> findAllAnnotations(Class<?> clazz) {
        if (clazz == Object.class) {
            return Collections.emptyList();
        }

        List<Annotation> annotations = new ArrayList<>();
        Optional.ofNullable(clazz.getSuperclass()).ifPresent(c -> annotations.addAll(findAllAnnotations(c)));
        Optional.ofNullable(clazz.getEnclosingClass()).ifPresent(c -> annotations.addAll(findAllAnnotations(c)));
        annotations.addAll(Arrays.asList(clazz.getAnnotations()));
        return annotations;
    }

    public static List<Field> findAllFields(Class<?> clazz) {
        if (clazz == Object.class) {
            return Collections.emptyList();
        }

        List<Field> fields = new ArrayList<>();
        Optional.ofNullable(clazz.getSuperclass()).ifPresent(c -> fields.addAll(findAllFields(c)));
        Optional.ofNullable(clazz.getEnclosingClass()).ifPresent(c -> fields.addAll(findAllFields(c)));
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
