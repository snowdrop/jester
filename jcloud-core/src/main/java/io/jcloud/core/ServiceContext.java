package io.jcloud.core;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.Service;
import io.jcloud.configuration.BaseConfigurationBuilder;
import io.jcloud.configuration.ServiceConfiguration;
import io.jcloud.configuration.ServiceConfigurationBuilder;
import io.jcloud.configuration.ServiceConfigurationLoader;

public final class ServiceContext {

    private final Service owner;
    private final JCloudContext jcloudContext;
    private final Path serviceFolder;
    private final Map<String, Object> store = new HashMap<>();
    private final ServiceConfiguration configuration;
    private final List<Object> customConfiguration = new ArrayList<>();

    public ServiceContext(Service owner, JCloudContext jcloudContext) {
        this.owner = owner;
        this.jcloudContext = jcloudContext;
        this.serviceFolder = Path.of("target", jcloudContext.getRunningTestClassName(), getName());
        this.configuration = ServiceConfigurationLoader.load(owner.getName(), jcloudContext,
                new ServiceConfigurationBuilder());
    }

    public Service getOwner() {
        return owner;
    }

    public String getName() {
        return owner.getName();
    }

    public JCloudContext getJCloudContext() {
        return jcloudContext;
    }

    public ExtensionContext getTestContext() {
        return jcloudContext.getTestContext();
    }

    public Path getServiceFolder() {
        return serviceFolder;
    }

    public ServiceConfiguration getConfiguration() {
        return configuration;
    }

    public <T> T getConfigurationAs(Class<T> configurationClazz) {
        return customConfiguration.stream().filter(configurationClazz::isInstance).map(configurationClazz::cast)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No found configuration for " + configurationClazz));
    }

    public void put(String key, Object value) {
        store.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) store.get(key);
    }

    public Map<String, Object> getAllProperties() {
        Map<String, Object> allProperties = new HashMap<>();
        // from store
        allProperties.putAll(store);
        // from runtime properties
        allProperties.putAll(owner.getProperties());
        return allProperties;
    }

    public <T extends Annotation, C> void loadCustomConfiguration(Class<C> clazz,
            BaseConfigurationBuilder<T, C> builder) {
        if (customConfiguration.stream().anyMatch(c -> c.getClass() == clazz)) {
            throw new RuntimeException("Multiple custom configuration loading for: " + clazz);
        }

        customConfiguration.add(ServiceConfigurationLoader.load(owner.getName(), jcloudContext, builder));
    }
}
