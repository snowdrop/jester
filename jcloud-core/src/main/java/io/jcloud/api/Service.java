package io.jcloud.api;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ServiceContext;
import io.jcloud.utils.LogsVerifier;

public interface Service extends ExtensionContext.Store.CloseableResource {

    String getContextId();

    String getName();

    String getDisplayName();

    io.jcloud.configuration.ServiceConfiguration getConfiguration();

    Map<String, String> getProperties();

    String getProperty(String property, String defaultValue);

    default Optional<String> getProperty(String property) {
        return Optional.ofNullable(getProperty(property, null));
    }

    List<String> getLogs();

    ServiceContext register(String serviceName, JCloudContext context);

    void init(ManagedResource resource);

    void start();

    void stop();

    String getHost();

    int getFirstMappedPort();

    int getMappedPort(int port);

    @Override
    void close();

    boolean isRunning();

    boolean isAutoStart();

    Service withProperty(String key, String value);

    default LogsVerifier logs() {
        return new LogsVerifier(this);
    }

    default void restart() {
        stop();
        start();
    }

    default void validate(AnnotationBinding binding, Annotation[] annotations) {

    }
}
