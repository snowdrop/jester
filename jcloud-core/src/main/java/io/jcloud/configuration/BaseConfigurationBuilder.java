package io.jcloud.configuration;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import io.jcloud.core.ScenarioContext;
import io.jcloud.utils.DurationUtils;
import io.jcloud.utils.PropertiesUtils;

public abstract class BaseConfigurationBuilder<T extends Annotation, C> {
    private static final String COMMA = ",";

    private Map<String, String> properties = Collections.emptyMap();
    private Optional<T> annotationConfig = Optional.empty();

    public BaseConfigurationBuilder<T, C> with(String serviceName, ScenarioContext scenarioContext) {
        this.annotationConfig = getAnnotationConfig(serviceName, scenarioContext);
        return this;
    }

    public BaseConfigurationBuilder<T, C> withProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public abstract C build();

    protected abstract Optional<T> getAnnotationConfig(String serviceName, ScenarioContext scenarioContext);

    protected Optional<Duration> loadDuration(String propertyKey, Function<T, String> annotationMapper) {
        // first annotation,
        if (annotationConfig.isPresent()) {
            String value = annotationMapper.apply(annotationConfig.get());
            if (StringUtils.isNotBlank(value)) {
                return Optional.of(DurationUtils.parse(value));
            }
        }

        // it not found in annotation, then property
        return PropertiesUtils.getAsDuration(properties, propertyKey);
    }

    protected Optional<Boolean> loadBoolean(String propertyKey, Function<T, Boolean> annotationMapper) {
        // first annotation,
        if (annotationConfig.isPresent()) {
            return Optional.of(annotationMapper.apply(annotationConfig.get()));
        }

        // it not found in annotation, then property
        return PropertiesUtils.getAsBoolean(properties, propertyKey);
    }

    protected Optional<String[]> loadArrayOfStrings(String propertyKey, Function<T, String[]> annotationMapper) {
        // first annotation,
        if (annotationConfig.isPresent()) {
            return Optional.of(annotationMapper.apply(annotationConfig.get()));
        }

        // it not found in annotation, then property
        return PropertiesUtils.get(properties, propertyKey).map(v -> v.trim().split(COMMA));
    }

    protected Optional<String> loadString(String propertyKey, Function<T, String> annotationMapper) {
        // first annotation,
        if (annotationConfig.isPresent()) {
            return Optional.of(annotationMapper.apply(annotationConfig.get()));
        }

        // it not found in annotation, then property
        return PropertiesUtils.get(properties, propertyKey);
    }

    protected Optional<Integer> loadInteger(String propertyKey, Function<T, Integer> annotationMapper) {
        // first annotation,
        if (annotationConfig.isPresent()) {
            return Optional.of(annotationMapper.apply(annotationConfig.get()));
        }

        // it not found in annotation, then property
        return PropertiesUtils.get(properties, propertyKey).filter(StringUtils::isNotEmpty).map(Integer::parseInt);
    }

    protected Optional<Double> loadDouble(String propertyKey, Function<T, Double> annotationMapper) {
        // first annotation,
        if (annotationConfig.isPresent()) {
            return Optional.of(annotationMapper.apply(annotationConfig.get()));
        }

        // it not found in annotation, then property
        return PropertiesUtils.get(properties, propertyKey).filter(StringUtils::isNotEmpty).map(Double::parseDouble);
    }
}
