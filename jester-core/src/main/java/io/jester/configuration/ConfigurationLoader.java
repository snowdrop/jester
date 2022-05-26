package io.jester.configuration;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import io.jester.core.JesterContext;

public final class ConfigurationLoader {
    private static final String PREFIX_TEMPLATE = "ts.%s.";

    private ConfigurationLoader() {

    }

    public static <T extends Annotation, C> C load(String target, JesterContext context,
            BaseConfigurationBuilder<T, C> builder) {
        Map<String, String> properties = new HashMap<>();
        // Then, highest priority: properties from system properties and scope as service name
        properties.putAll(loadPropertiesFromSystemProperties(target));

        // Load configuration from annotations
        builder.with(target, context).withProperties(properties);

        // Build service configuration mixing up configuration from properties and annotations
        return builder.build();
    }

    private static Map<String, String> loadPropertiesFromSystemProperties(String scope) {
        return loadPropertiesFrom(System.getProperties(), scope);
    }

    private static Map<String, String> loadPropertiesFrom(Properties prop, String scope) {
        Map<String, String> properties = new HashMap<>();
        String prefix = String.format(PREFIX_TEMPLATE, scope);
        for (Entry<Object, Object> entry : prop.entrySet()) {
            String key = (String) entry.getKey();
            if (StringUtils.startsWith(key, prefix)) {
                properties.put(key.replace(prefix, StringUtils.EMPTY), (String) entry.getValue());
            }
        }

        return properties;
    }
}
