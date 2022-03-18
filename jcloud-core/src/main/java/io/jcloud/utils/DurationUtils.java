package io.jcloud.utils;

import java.time.Duration;

public final class DurationUtils {
    private DurationUtils() {

    }

    public static Duration parse(String value) {
        if (Character.isDigit(value.charAt(0))) {
            value = "PT" + value;
        }

        return Duration.parse(value);
    }
}
