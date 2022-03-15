package io.jcloud.resources.quarkus;

import java.util.Arrays;
import java.util.List;

import io.jcloud.api.model.QuarkusLaunchMode;
import io.jcloud.configuration.PropertyLookup;
import io.jcloud.core.ServiceContext;
import io.jcloud.logging.LoggingHandler;
import io.jcloud.utils.QuarkusUtils;

public class QuarkusProxy {
    private static final String EXPECTED_OUTPUT_DEFAULT = "Installed features";
    private static final PropertyLookup EXPECTED_OUTPUT = new PropertyLookup("quarkus.expected.log",
            EXPECTED_OUTPUT_DEFAULT);
    private static final List<String> ERRORS = Arrays.asList("Failed to start application",
            "Failed to load config value of type class",
            "Quarkus may already be running or the port is used by another application",
            "One or more configuration errors have prevented the application from starting",
            "Attempting to start live reload endpoint to recover from previous Quarkus startup failure",
            "Dev mode process did not complete successfully");

    protected final ServiceContext context;
    private final QuarkusLaunchMode launchMode;

    public QuarkusProxy(ServiceContext context) {
        this.context = context;
        this.launchMode = detectLaunchMode(context);

        configureLogging();
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
        return EXPECTED_OUTPUT.get(context);
    }

    private void configureLogging() {
        context.getOwner().withProperty("quarkus.log.console.format", "%d{HH:mm:ss,SSS} %s%e%n");
    }

    private static QuarkusLaunchMode detectLaunchMode(ServiceContext serviceContext) {
        QuarkusLaunchMode launchMode = QuarkusLaunchMode.JVM;
        if (QuarkusUtils.isNativePackageType(serviceContext)) {
            launchMode = QuarkusLaunchMode.NATIVE;
        } else if (QuarkusUtils.isLegacyJarPackageType(serviceContext)) {
            launchMode = QuarkusLaunchMode.LEGACY_JAR;
        }

        return launchMode;
    }
}
