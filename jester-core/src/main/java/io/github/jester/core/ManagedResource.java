package io.github.jester.core;

import static io.github.jester.utils.AwaitilityUtils.untilIsTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.jester.logging.LoggingHandler;
import io.github.jester.utils.AwaitilitySettings;
import io.github.jester.utils.PropertiesUtils;

public abstract class ManagedResource {

    protected static final String APPLICATION_PROPERTIES = "application.properties";
    protected static final Path SOURCE_RESOURCES = Path.of("src", "main", "resources");
    protected static final Path SOURCE_TEST_RESOURCES = Path.of("src", "test", "resources");

    protected ServiceContext context;

    /**
     * @return name of the running resource.
     */
    public abstract String getDisplayName();

    /**
     * Start the resource. If the resource is already started, it will do nothing.
     *
     * @throws RuntimeException
     *             when application errors at startup.
     */
    public abstract void start();

    /**
     * Stop the resource.
     */
    public abstract void stop();

    /**
     * Get the Host of the running resource.
     */
    public abstract String getHost();

    /**
     * Get the first mapped port.
     */
    public abstract int getFirstMappedPort();

    /**
     * Get the Port of the running resource.
     */
    public abstract int getMappedPort(int port);

    /**
     * @return if the resource is running.
     */
    public abstract boolean isRunning();

    /**
     * @return the logging handler associated with the managed resource.
     */
    protected abstract LoggingHandler getLoggingHandler();

    /**
     * @return if the resource has failed.
     */
    public boolean isFailed() {
        return false;
    }

    /**
     * @return the list of logs.
     */
    public List<String> logs() {
        return getLoggingHandler().logs();
    }

    /**
     * @return the computed property directly from the managed resource.
     */
    public String getProperty(String property) {
        Map<String, Object> computedProperties = getAllComputedProperties();
        Object value = Optional.ofNullable(computedProperties.get(property))
                .orElseGet(() -> computedProperties.get(propertyWithProfile(property)));
        return value != null ? String.valueOf(value) : null;
    }

    public void validate() {

    }

    protected void init(ServiceContext context) {
        this.context = context;
    }

    protected void waitUntilResourceIsStarted() {
        Duration startupCheckInterval = context.getConfiguration().getStartupCheckPollInterval();
        Duration startupTimeout = context.getConfiguration().getStartupTimeout();
        untilIsTrue(this::isRunningOrFailed,
                AwaitilitySettings.using(startupCheckInterval, startupTimeout).doNotIgnoreExceptions()
                        .withService(context.getOwner())
                        .timeoutMessage("Service didn't start in %s minutes", startupTimeout));
        if (getLoggingHandler() != null) {
            getLoggingHandler().flush();
        }
    }

    protected Map<String, Object> getAllComputedProperties() {
        Map<String, Object> allProperties = new HashMap<>();
        // from properties file
        allProperties.putAll(getPropertiesFromFile());
        // from context
        allProperties.putAll(context.getAllProperties());
        return allProperties;
    }

    protected Path getComputedApplicationProperties() {
        return context.getServiceFolder().resolve(APPLICATION_PROPERTIES);
    }

    private Map<String, String> getPropertiesFromFile() {
        List<Path> applicationPropertiesCandidates = Arrays.asList(getComputedApplicationProperties(),
                SOURCE_TEST_RESOURCES.resolve(APPLICATION_PROPERTIES),
                SOURCE_RESOURCES.resolve(APPLICATION_PROPERTIES));

        return applicationPropertiesCandidates.stream().filter(Files::exists).map(PropertiesUtils::toMap).findFirst()
                .orElseGet(Collections::emptyMap);
    }

    private String propertyWithProfile(String name) {
        return "%" + context.getJesterContext().getRunningTestClassName() + "." + name;
    }

    private boolean isRunningOrFailed() {
        if (isFailed()) {
            stop();
            throw new RuntimeException("Resource failed to start");
        }

        return isRunning();
    }
}
