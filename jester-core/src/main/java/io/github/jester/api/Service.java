package io.github.jester.api;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.github.jester.api.extensions.AnnotationBinding;
import io.github.jester.configuration.ServiceConfiguration;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.ServiceContext;
import io.github.jester.utils.LogsVerifier;

public interface Service extends ExtensionContext.Store.CloseableResource {

    String getContextId();

    String getName();

    String getDisplayName();

    ServiceConfiguration getConfiguration();

    Map<String, String> getProperties();

    String getProperty(String property, String defaultValue);

    default Optional<String> getProperty(String property) {
        return Optional.ofNullable(getProperty(property, null));
    }

    List<String> getLogs();

    ServiceContext register(String serviceName, JesterContext context);

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
