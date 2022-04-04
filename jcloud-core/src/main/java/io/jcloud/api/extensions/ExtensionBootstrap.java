package io.jcloud.api.extensions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.jcloud.api.Service;
import io.jcloud.core.DependencyContext;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ServiceContext;

public interface ExtensionBootstrap {

    boolean appliesFor(JCloudContext context);

    default void beforeAll(JCloudContext context) {

    }

    default void afterAll(JCloudContext context) {

    }

    default void beforeEach(JCloudContext context) {

    }

    default void afterEach(JCloudContext context) {

    }

    default void onSuccess(JCloudContext context) {

    }

    default void onDisabled(JCloudContext context, Optional<String> reason) {

    }

    default void onError(JCloudContext context, Throwable throwable) {

    }

    default void onServiceLaunch(JCloudContext context, Service service) {

    }

    default void updateContext(JCloudContext context) {

    }

    default void updateServiceContext(ServiceContext context) {

    }

    default List<Class<?>> supportedParameters() {
        return Collections.emptyList();
    }

    default Optional<Object> getParameter(DependencyContext dependency) {
        return Optional.empty();
    }
}
