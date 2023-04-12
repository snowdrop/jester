package io.github.snowdrop.jester.configuration;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.utils.DurationUtils;
import io.github.snowdrop.jester.utils.PropertiesUtils;

public abstract class BaseConfigurationBuilder<T extends Annotation, C> {
    private static final String COMMA = ",";

    private Map<String, String> properties = Collections.emptyMap();
    private Optional<T> annotationConfig = Optional.empty();

    public BaseConfigurationBuilder<T, C> with(String serviceName, JesterContext context) {
        this.annotationConfig = getAnnotationConfig(serviceName, context);
        return this;
    }

    public BaseConfigurationBuilder<T, C> withProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public abstract C build();

    protected abstract Optional<T> getAnnotationConfig(String serviceName, JesterContext context);

    protected Optional<Duration> loadDuration(String propertyKey, Function<T, String> annotationMapper) {
        return PropertiesUtils.getAsDuration(properties, propertyKey).or(() -> annotationConfig
                .map(annotationMapper::apply).filter(StringUtils::isNotBlank).map(DurationUtils::parse));
    }

    protected Optional<Boolean> loadBoolean(String propertyKey, Function<T, Boolean> annotationMapper) {
        return PropertiesUtils.getAsBoolean(properties, propertyKey)
                .or(() -> annotationConfig.map(annotationMapper::apply));
    }

    protected Optional<String[]> loadArrayOfStrings(String propertyKey, Function<T, String[]> annotationMapper) {
        return PropertiesUtils.get(properties, propertyKey).map(v -> v.trim().split(COMMA))
                .or(() -> annotationConfig.map(annotationMapper::apply));
    }

    protected Optional<String> loadString(String propertyKey, Function<T, String> annotationMapper) {
        return PropertiesUtils.get(properties, propertyKey).or(() -> annotationConfig.map(annotationMapper::apply));
    }

    protected <E extends Enum<E>> Optional<E> loadEnum(String propertyKey, Class<E> enumType,
            Function<T, E> annotationMapper) {
        return PropertiesUtils.get(properties, propertyKey).map(s -> Enum.valueOf(enumType, s))
                .or(() -> annotationConfig.map(annotationMapper::apply));
    }

    protected Optional<int[]> loadArrayOfIntegers(String propertyKey, Function<T, int[]> annotationMapper) {
        return PropertiesUtils.get(properties, propertyKey).map(v -> v.trim().split(COMMA)).map(arrayOfStrings -> {
            int[] arrayOfIntegers = new int[arrayOfStrings.length];
            for (int i = 0; i < arrayOfStrings.length; i++) {
                arrayOfIntegers[i] = Integer.parseInt(arrayOfStrings[i]);
            }

            return arrayOfIntegers;
        }).or(() -> annotationConfig.map(annotationMapper::apply));
    }

    protected Optional<Integer> loadInteger(String propertyKey, Function<T, Integer> annotationMapper) {
        return PropertiesUtils.get(properties, propertyKey).filter(StringUtils::isNotEmpty).map(Integer::parseInt)
                .or(() -> annotationConfig.map(annotationMapper::apply));
    }

    protected Optional<Double> loadDouble(String propertyKey, Function<T, Double> annotationMapper) {
        return PropertiesUtils.get(properties, propertyKey).filter(StringUtils::isNotEmpty).map(Double::parseDouble)
                .or(() -> annotationConfig.map(annotationMapper::apply));
    }
}
