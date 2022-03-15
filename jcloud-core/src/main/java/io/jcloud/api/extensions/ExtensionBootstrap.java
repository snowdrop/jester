package io.jcloud.api.extensions;

import java.util.Optional;

import io.jcloud.api.Service;
import io.jcloud.core.ScenarioContext;
import io.jcloud.core.ServiceContext;

public interface ExtensionBootstrap {

    boolean appliesFor(ScenarioContext context);

    default void beforeAll(ScenarioContext context) {

    }

    default void afterAll(ScenarioContext context) {

    }

    default void beforeEach(ScenarioContext context) {

    }

    default void afterEach(ScenarioContext context) {

    }

    default void onSuccess(ScenarioContext context) {

    }

    default void onDisabled(ScenarioContext context, Optional<String> reason) {

    }

    default void onError(ScenarioContext context, Throwable throwable) {

    }

    default void onServiceLaunch(ScenarioContext context, Service service) {

    }

    default void updateServiceContext(ServiceContext context) {

    }

    default Optional<Object> getParameter(Class<?> clazz) {
        return Optional.empty();
    }
}