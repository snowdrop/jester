package io.jcloud.resources.quarkus;

import java.util.Arrays;
import java.util.List;

import io.jcloud.api.model.QuarkusLaunchMode;
import io.jcloud.configuration.PropertyLookup;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ServiceContext;
import io.jcloud.utils.QuarkusUtils;

public abstract class QuarkusManagedResource extends ManagedResource {
    private static final String EXPECTED_OUTPUT_DEFAULT = "Installed features";
    private static final PropertyLookup EXPECTED_OUTPUT = new PropertyLookup("quarkus.expected.log",
            EXPECTED_OUTPUT_DEFAULT);
    private static final List<String> ERRORS = Arrays.asList("Failed to start application",
            "Failed to load config value of type class",
            "Quarkus may already be running or the port is used by another application",
            "One or more configuration errors have prevented the application from starting",
            "Attempting to start live reload endpoint to recover from previous Quarkus startup failure",
            "Dev mode process did not complete successfully");

    protected QuarkusLaunchMode launchMode;

    @Override
    public String getDisplayName() {
        return String.format("Quarkus %s mode", launchMode);
    }

    @Override
    public boolean isRunning() {
        return getLoggingHandler() != null && getLoggingHandler().logsContains(EXPECTED_OUTPUT.get(context));
    }

    @Override
    public boolean isFailed() {
        return getLoggingHandler() != null
                && ERRORS.stream().anyMatch(error -> getLoggingHandler().logsContains(error));
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        this.launchMode = detectLaunchMode(context);
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
