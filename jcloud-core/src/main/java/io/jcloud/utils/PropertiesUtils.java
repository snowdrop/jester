package io.jcloud.utils;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public final class PropertiesUtils {

    public static final Path TARGET = Path.of("target");
    public static final String SLASH = "/";

    public static final String RESOURCE_PREFIX = "resource::/";
    public static final String SECRET_PREFIX = "secret::/";

    private static final String PROPERTY_START_TAG = "${";
    private static final String PROPERTY_END_TAG = "}";
    private static final String PROPERTY_WITH_OPTIONAL = ":";

    private PropertiesUtils() {

    }

    /**
     * Try to resolve the value property from the value if the content is contained between ${xxx}.
     *
     * @param value
     * @return
     */
    public static String resolveProperty(String value) {
        if (StringUtils.startsWith(value, PROPERTY_START_TAG)) {
            String propertyKey = StringUtils.substringBetween(value, PROPERTY_START_TAG, PROPERTY_END_TAG);
            String defaultValue = StringUtils.EMPTY;
            int optionalIndex = propertyKey.indexOf(PROPERTY_WITH_OPTIONAL);
            if (optionalIndex > 0) {
                defaultValue = propertyKey.substring(optionalIndex + 1);
                propertyKey = propertyKey.substring(0, optionalIndex);
            }

            return System.getProperty(propertyKey, defaultValue);
        }

        return value;
    }

    public static Map<String, String> toMap(String propertiesFile) {
        try (InputStream in = ClassLoader.getSystemResourceAsStream(propertiesFile)) {
            return toMap(in);
        } catch (IOException e) {
            fail("Could not load map from system resource. Caused by " + e);
        }

        return Collections.emptyMap();
    }

    public static Map<String, String> toMap(InputStream is) {
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            fail("Could not load map. Caused by " + e);
        }

        return (Map) properties;
    }

}