package io.jcloud.configuration;

public final class SpringServiceConfiguration {
    private String expectedLog = "initialization completed";

    public String getExpectedLog() {
        return expectedLog;
    }

    public void setExpectedLog(String expectedLog) {
        this.expectedLog = expectedLog;
    }
}
