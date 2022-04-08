package io.jcloud.core;

import static io.jcloud.utils.AwaitilityUtils.untilIsTrue;

import java.time.Duration;
import java.util.List;

import io.jcloud.logging.LoggingHandler;
import io.jcloud.utils.AwaitilityUtils;

public abstract class ManagedResource {

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
        return context.get(property);
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
                AwaitilityUtils.AwaitilitySettings.using(startupCheckInterval, startupTimeout).doNotIgnoreExceptions()
                        .withService(context.getOwner())
                        .timeoutMessage("Service didn't start in %s minutes", startupTimeout));
        if (getLoggingHandler() != null) {
            getLoggingHandler().flush();
        }
    }

    private boolean isRunningOrFailed() {
        if (isFailed()) {
            stop();
            throw new RuntimeException("Resource failed to start");
        }

        return isRunning();
    }
}
