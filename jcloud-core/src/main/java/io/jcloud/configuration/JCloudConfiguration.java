package io.jcloud.configuration;

public final class JCloudConfiguration {
    private String target;
    private boolean profilingEnabled;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isProfilingEnabled() {
        return profilingEnabled;
    }

    public void setProfilingEnabled(boolean profilingEnabled) {
        this.profilingEnabled = profilingEnabled;
    }
}
