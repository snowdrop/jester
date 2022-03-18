package io.jcloud.configuration;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import io.jcloud.core.ScenarioContext;

public final class ServiceConfigurationLoader {

    private static final String GLOBAL_PROPERTIES = System.getProperty("ts.test.resources.file.location",
            "global.properties");
    private static final String TEST_PROPERTIES = "test.properties";
    private static final String PREFIX_TEMPLATE = "ts.%s.";
    private static final String GLOBAL_SCOPE = "global";

    private ServiceConfigurationLoader() {

    }

    public static <T extends Annotation, C> C load(String scope, ScenarioContext scenarioContext,
            BaseConfigurationBuilder<T, C> builder) {
        Map<String, String> properties = new HashMap<>();
        if (GLOBAL_SCOPE.equals(scope)) {
            // Lowest priority: properties from global.properties and scope `global`
            properties.putAll(loadPropertiesFrom(GLOBAL_PROPERTIES, GLOBAL_SCOPE));
        }

        // Then, properties from test.properties and scope as service name
        properties.putAll(loadPropertiesFrom(TEST_PROPERTIES, scope));
        // Then, highest priority: properties from system properties and scope as service name
        properties.putAll(loadPropertiesFromSystemProperties(scope));

        // Load configuration from annotations
        builder.with(scope, scenarioContext).withProperties(properties);

        // Build service configuration mixing up configuration from properties and annotations
        return builder.build();
    }

    private static Map<String, String> loadPropertiesFromSystemProperties(String scope) {
        return loadPropertiesFrom(System.getProperties(), scope);
    }

    private static Map<String, String> loadPropertiesFrom(String propertiesFile, String scope) {
        try (InputStream input = ServiceConfigurationLoader.class.getClassLoader()
                .getResourceAsStream(propertiesFile)) {
            Properties prop = new Properties();
            prop.load(input);
            return loadPropertiesFrom(prop, scope);
        } catch (Exception ignored) {
            // There is no properties file: this is not mandatory.
        }

        return Collections.emptyMap();
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
