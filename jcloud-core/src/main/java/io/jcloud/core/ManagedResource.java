package io.jcloud.core;

import static io.jcloud.utils.AwaitilityUtils.untilIsTrue;

import java.time.Duration;
import java.util.List;

import io.jcloud.logging.LoggingHandler;
import io.jcloud.utils.AwaitilityUtils;

public abstract class ManagedResource {

    protected static final String SERVICE_STARTUP_TIMEOUT = "startup.timeout";
    protected static final Duration SERVICE_STARTUP_TIMEOUT_DEFAULT = Duration.ofMinutes(5);
    private static final String SERVICE_STARTUP_CHECK_POLL_INTERVAL = "startup.check-poll-interval";
    private static final Duration SERVICE_STARTUP_CHECK_POLL_INTERVAL_DEFAULT = Duration.ofSeconds(2);

    protected ServiceContext context;

    /**
     * @return name of the running resource.
     */
    public abstract String getDisplayName();

    /**
     * Start the resource. If the resource is already started, it will do nothing.
     *
     * @throws RuntimeException when application errors at startup.
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
     * Get the Port of the running resource.
     */
    public abstract int getMappedPort(int port);

    /**
     * @return if the resource is running.
     */
    public abstract boolean isRunning();

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

    protected abstract LoggingHandler getLoggingHandler();

    protected void init(ServiceContext context) {
        this.context = context;
    }

    protected void waitUntilResourceIsStarted() {
        Duration startupCheckInterval = context.getOwner().getConfiguration()
                .getAsDuration(SERVICE_STARTUP_CHECK_POLL_INTERVAL, SERVICE_STARTUP_CHECK_POLL_INTERVAL_DEFAULT);
        Duration startupTimeout = context.getOwner().getConfiguration()
                .getAsDuration(SERVICE_STARTUP_TIMEOUT, SERVICE_STARTUP_TIMEOUT_DEFAULT);
        untilIsTrue(this::isRunningOrFailed, AwaitilityUtils.AwaitilitySettings
                .using(startupCheckInterval, startupTimeout)
                .doNotIgnoreExceptions()
                .withService(context.getOwner())
                .timeoutMessage("Service didn't start in %s minutes", startupTimeout));
        getLoggingHandler().flush();
    }

    private boolean isRunningOrFailed() {
        if (isFailed()) {
            stop();
            throw new RuntimeException("Resource failed to start");
        }

        return isRunning();
    }
}
