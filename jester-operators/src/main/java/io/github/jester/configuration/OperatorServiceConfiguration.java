package io.github.jester.configuration;

import java.time.Duration;

public final class OperatorServiceConfiguration {
    private Duration installTimeout = Duration.ofMinutes(5);

    public Duration getInstallTimeout() {
        return installTimeout;
    }

    public void setInstallTimeout(Duration installTimeout) {
        this.installTimeout = installTimeout;
    }
}
