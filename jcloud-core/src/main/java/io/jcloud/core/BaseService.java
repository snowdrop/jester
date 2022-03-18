package io.jcloud.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import io.jcloud.api.HookAction;
import io.jcloud.api.Service;
import io.jcloud.api.ServiceListener;
import io.jcloud.configuration.ServiceConfiguration;
import io.jcloud.logging.Log;
import io.jcloud.utils.FileUtils;
import io.jcloud.utils.PropertiesUtils;

public class BaseService<T extends Service> implements Service {
    private final ServiceLoader<ServiceListener> listeners = ServiceLoader.load(ServiceListener.class);

    private final List<HookAction> onPreStartHookActions = new LinkedList<>();
    private final List<HookAction> onPostStartHookActions = new LinkedList<>();
    private final Map<String, String> properties = new HashMap<>();
    private final List<Runnable> futureProperties = new LinkedList<>();

    private ManagedResource managedResource;
    private String serviceName;
    private ServiceContext context;
    private boolean autoStart = true;

    @Override
    public String getScenarioId() {
        return context.getScenarioId();
    }

    @Override
    public String getName() {
        return serviceName;
    }

    @Override
    public String getDisplayName() {
        return managedResource.getDisplayName();
    }

    @Override
    public boolean isAutoStart() {
        return autoStart;
    }

    public T onPreStart(HookAction hookAction) {
        onPreStartHookActions.add(hookAction);
        return (T) this;
    }

    public T onPostStart(HookAction hookAction) {
        onPostStartHookActions.add(hookAction);
        return (T) this;
    }

    public T setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
        return (T) this;
    }

    /**
     * The runtime configuration property to be used if the built artifact is configured to be run.
     */
    public T withProperties(String... propertiesFiles) {
        properties.clear();
        Stream.of(propertiesFiles).map(PropertiesUtils::toMap).forEach(properties::putAll);
        return (T) this;
    }

    /**
     * The runtime configuration property to be used if the built artifact is configured to be run.
     */
    @Override
    public T withProperty(String key, String value) {
        this.properties.put(key, value);
        return (T) this;
    }

    /**
     * The runtime configuration property to be used if the built artifact is configured to be run.
     */
    public T withProperty(String key, Supplier<String> value) {
        futureProperties.add(() -> properties.put(key, value.get()));
        return (T) this;
    }

    @Override
    public boolean isRunning() {
        if (managedResource == null) {
            return false;
        }

        return managedResource.isRunning();
    }

    @Override
    public String getHost() {
        return managedResource.getHost();
    }

    @Override
    public int getMappedPort(int port) {
        return managedResource.getMappedPort(port);
    }

    @Override
    public ServiceConfiguration getConfiguration() {
        return context.getConfiguration();
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public List<String> getLogs() {
        return new ArrayList<>(managedResource.logs());
    }

    @Override
    public String getProperty(String property, String defaultValue) {
        String value = getProperties().get(property);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        String computedValue = managedResource.getProperty(property);
        if (StringUtils.isNotBlank(computedValue)) {
            return computedValue;
        }

        return defaultValue;
    }

    /**
     * Start the managed resource. If the managed resource is running, it does nothing.
     *
     * @throws RuntimeException
     *             when application errors at startup.
     */
    @Override
    public void start() {
        if (isRunning()) {
            return;
        }

        Log.debug(this, "Starting service (%s)", getDisplayName());
        onPreStartHookActions.forEach(a -> a.handle(this));
        doStart();
        onPostStartHookActions.forEach(a -> a.handle(this));
        Log.info(this, "Service started (%s)", getDisplayName());
    }

    /**
     * Stop the application.
     */
    @Override
    public void stop() {
        if (!isRunning()) {
            return;
        }

        Log.debug(this, "Stopping service (%s)", getDisplayName());
        listeners.forEach(ext -> ext.onServiceStopped(context));
        managedResource.stop();

        Log.info(this, "Service stopped (%s)", getDisplayName());
    }

    /**
     * Let JUnit close remaining resources.
     */
    @Override
    public void close() {
        if (!context.getScenarioContext().isDebug()) {
            stop();
            if (context.getConfiguration().isDeleteFolderOnClose()) {
                try {
                    FileUtils.deletePath(getServiceFolder());
                } catch (Exception ex) {
                    Log.warn(this, "Could not delete service folder. Caused by " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public ServiceContext register(String serviceName, ScenarioContext context) {
        this.serviceName = serviceName;
        this.context = new ServiceContext(this, context);
        onPreStart(s -> futureProperties.forEach(Runnable::run));
        context.getTestStore().put(serviceName, this);
        return this.context;
    }

    @Override
    public void init(ManagedResource managedResource) {
        this.managedResource = managedResource;
        FileUtils.recreateDirectory(context.getServiceFolder());
        this.managedResource.init(context);
        this.managedResource.validate();
    }

    public Path getServiceFolder() {
        return context.getServiceFolder();
    }

    private void doStart() {
        try {
            managedResource.start();
            managedResource.waitUntilResourceIsStarted();
            listeners.forEach(ext -> ext.onServiceStarted(context));
        } catch (Exception ex) {
            listeners.forEach(ext -> ext.onServiceError(context, ex));
            throw ex;
        }
    }
}
