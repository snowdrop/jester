package io.github.jester.configuration;

import java.time.Duration;
import java.util.logging.Level;

import io.github.jester.api.PortResolutionStrategy;

@SuppressWarnings("checkstyle:MagicNumber")
public final class ServiceConfiguration {

    private Duration startupTimeout = Duration.ofMinutes(5);
    private Duration startupCheckPollInterval = Duration.ofSeconds(2);
    private Double factorTimeout = 1.0;
    private boolean deleteFolderOnClose = true;
    private boolean logEnabled = true;
    private Level logLevel = Level.INFO;
    private int portRangeMin = 1100;
    private int portRangeMax = 49151;
    private PortResolutionStrategy portResolutionStrategy = PortResolutionStrategy.INCREMENTAL;
    private String imageRegistry = "localhost:5000";

    public Duration getStartupTimeout() {
        return startupTimeout;
    }

    public void setStartupTimeout(Duration startupTimeout) {
        this.startupTimeout = startupTimeout;
    }

    public Duration getStartupCheckPollInterval() {
        return startupCheckPollInterval;
    }

    public void setStartupCheckPollInterval(Duration startupCheckPollInterval) {
        this.startupCheckPollInterval = startupCheckPollInterval;
    }

    public Double getFactorTimeout() {
        return factorTimeout;
    }

    public void setFactorTimeout(Double factorTimeout) {
        this.factorTimeout = factorTimeout;
    }

    public boolean isDeleteFolderOnClose() {
        return deleteFolderOnClose;
    }

    public void setDeleteFolderOnClose(boolean deleteFolderOnClose) {
        this.deleteFolderOnClose = deleteFolderOnClose;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    public int getPortRangeMin() {
        return portRangeMin;
    }

    public void setPortRangeMin(int portRangeMin) {
        this.portRangeMin = portRangeMin;
    }

    public int getPortRangeMax() {
        return portRangeMax;
    }

    public void setPortRangeMax(int portRangeMax) {
        this.portRangeMax = portRangeMax;
    }

    public PortResolutionStrategy getPortResolutionStrategy() {
        return portResolutionStrategy;
    }

    public void setPortResolutionStrategy(PortResolutionStrategy portResolutionStrategy) {
        this.portResolutionStrategy = portResolutionStrategy;
    }

    public String getImageRegistry() {
        return imageRegistry;
    }

    public void setImageRegistry(String imageRegistry) {
        this.imageRegistry = imageRegistry;
    }
}
