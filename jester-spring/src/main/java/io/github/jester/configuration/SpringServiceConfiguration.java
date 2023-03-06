package io.github.jester.configuration;

public final class SpringServiceConfiguration {
    private String expectedLog = "Started .* in .* seconds";

    public String getExpectedLog() {
        return expectedLog;
    }

    public void setExpectedLog(String expectedLog) {
        this.expectedLog = expectedLog;
    }
}
