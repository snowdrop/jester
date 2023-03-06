package io.github.snowdrop.jester.resources.quarkus.common;

import java.util.Arrays;
import java.util.List;

import io.github.snowdrop.jester.api.model.QuarkusLaunchMode;
import io.github.snowdrop.jester.configuration.QuarkusServiceConfiguration;
import io.github.snowdrop.jester.configuration.QuarkusServiceConfigurationBuilder;
import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.logging.LoggingHandler;
import io.github.snowdrop.jester.utils.QuarkusUtils;

public class QuarkusResource {
    private static final List<String> ERRORS = Arrays.asList("Failed to start application",
            "Failed to load config value of type class",
            "Quarkus may already be running or the port is used by another application",
            "One or more configuration errors have prevented the application from starting",
            "Attempting to start live reload endpoint to recover from previous Quarkus startup failure",
            "Dev mode process did not complete successfully");

    protected final ServiceContext context;
    private final QuarkusLaunchMode launchMode;

    public QuarkusResource(ServiceContext context) {
        this.context = context;
        this.launchMode = detectLaunchMode(context);
        context.loadCustomConfiguration(QuarkusServiceConfiguration.class, new QuarkusServiceConfigurationBuilder());
    }

    public String getDisplayName() {
        return String.format("Quarkus %s mode", launchMode);
    }

    public boolean isRunning(LoggingHandler loggingHandler) {
        return loggingHandler != null && loggingHandler.logsContains(getExpectedLog());
    }

    public boolean isFailed(LoggingHandler loggingHandler) {
        return loggingHandler != null && ERRORS.stream().anyMatch(error -> loggingHandler.logsContains(error));
    }

    public QuarkusLaunchMode getLaunchMode() {
        return launchMode;
    }

    public String getExpectedLog() {
        return context.getConfigurationAs(QuarkusServiceConfiguration.class).getExpectedLog();
    }

    private static QuarkusLaunchMode detectLaunchMode(ServiceContext context) {
        QuarkusLaunchMode launchMode = QuarkusLaunchMode.JVM;
        if (QuarkusUtils.isNativePackageType(context.getOwner())) {
            launchMode = QuarkusLaunchMode.NATIVE;
        } else if (QuarkusUtils.isLegacyJarPackageType(context.getOwner())) {
            launchMode = QuarkusLaunchMode.LEGACY_JAR;
        }

        return launchMode;
    }
}
