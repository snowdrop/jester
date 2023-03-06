package io.github.snowdrop.jester.utils;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;

import io.github.snowdrop.jester.api.Service;

public final class AwaitilitySettings {

    private static final int POLL_SECONDS = 1;
    private static final int TIMEOUT_SECONDS = 30;

    Duration interval = Duration.ofSeconds(POLL_SECONDS);
    Duration timeout = Duration.ofSeconds(TIMEOUT_SECONDS);
    Service service;
    String timeoutMessage = StringUtils.EMPTY;
    boolean doNotIgnoreExceptions = false;

    public static AwaitilitySettings defaults() {
        return new AwaitilitySettings();
    }

    public static AwaitilitySettings usingTimeout(Duration timeout) {
        AwaitilitySettings settings = defaults();
        settings.timeout = timeout;
        return settings;
    }

    public static AwaitilitySettings using(Duration interval, Duration timeout) {
        AwaitilitySettings settings = defaults();
        settings.interval = interval;
        settings.timeout = timeout;
        return settings;
    }

    public AwaitilitySettings withService(Service service) {
        this.service = service;
        return this;
    }

    public AwaitilitySettings timeoutMessage(String message, Object... args) {
        this.timeoutMessage = String.format(message, args);
        return this;
    }

    public AwaitilitySettings doNotIgnoreExceptions() {
        this.doNotIgnoreExceptions = true;
        return this;
    }
}
