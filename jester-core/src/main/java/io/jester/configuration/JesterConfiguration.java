package io.jester.configuration;

public final class JesterConfiguration {
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
