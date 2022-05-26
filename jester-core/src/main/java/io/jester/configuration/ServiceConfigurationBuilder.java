package io.jester.configuration;

import java.util.Optional;
import java.util.logging.Level;

import io.jester.api.PortResolutionStrategy;
import io.jester.core.JesterContext;

public class ServiceConfigurationBuilder
        extends BaseConfigurationBuilder<io.jester.api.ServiceConfiguration, ServiceConfiguration> {

    private static final String STARTUP_TIMEOUT = "startup.timeout";
    private static final String STARTUP_CHECK_POLL_INTERVAL = "startup.check-poll-interval";
    private static final String FACTOR_TIMEOUT_PROPERTY = "factor.timeout";
    private static final String DELETE_FOLDER_ON_CLOSE = "delete.folder.on.close";
    private static final String LOG_ENABLED = "log.enabled";
    private static final String LOG_LEVEL = "log.level";
    private static final String PORT_RANGE_MIN = "port.range.min";
    private static final String PORT_RANGE_MAX = "port.range.max";
    private static final String PORT_RESOLUTION_STRATEGY = "port.resolution.strategy";
    private static final String IMAGE_REGISTRY = "image.registry";

    @Override
    public ServiceConfiguration build() {
        ServiceConfiguration config = new ServiceConfiguration();
        loadDuration(STARTUP_TIMEOUT, a -> a.startupTimeout()).ifPresent(config::setStartupTimeout);
        loadDuration(STARTUP_CHECK_POLL_INTERVAL, a -> a.startupCheckPollInterval())
                .ifPresent(config::setStartupCheckPollInterval);
        loadDouble(FACTOR_TIMEOUT_PROPERTY, a -> a.factorTimeout()).ifPresent(config::setFactorTimeout);
        loadBoolean(DELETE_FOLDER_ON_CLOSE, a -> a.deleteFolderOnClose()).ifPresent(config::setDeleteFolderOnClose);
        loadBoolean(LOG_ENABLED, a -> a.logEnabled()).ifPresent(config::setLogEnabled);
        loadString(LOG_LEVEL, a -> a.logLevel()).map(Level::parse).ifPresent(config::setLogLevel);
        loadInteger(PORT_RANGE_MIN, a -> a.portRangeMin()).ifPresent(config::setPortRangeMin);
        loadInteger(PORT_RANGE_MAX, a -> a.portRangeMax()).ifPresent(config::setPortRangeMax);
        loadString(PORT_RESOLUTION_STRATEGY, a -> a.portResolutionStrategy()).map(String::toUpperCase)
                .map(PortResolutionStrategy::valueOf).ifPresent(config::setPortResolutionStrategy);
        loadString(IMAGE_REGISTRY, a -> a.imageRegistry()).ifPresent(config::setImageRegistry);
        return config;
    }

    @Override
    protected Optional<io.jester.api.ServiceConfiguration> getAnnotationConfig(String serviceName,
            JesterContext context) {
        return context.getAnnotatedConfiguration(io.jester.api.ServiceConfiguration.class,
                a -> a.forService().equals(serviceName));
    }
}
