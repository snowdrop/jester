package io.jester.api.extensions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.jester.api.Service;
import io.jester.core.DependencyContext;
import io.jester.core.JesterContext;
import io.jester.core.ServiceContext;

public interface ExtensionBootstrap {

    boolean appliesFor(JesterContext context);

    default void beforeAll(JesterContext context) {

    }

    default void afterAll(JesterContext context) {

    }

    default void beforeEach(JesterContext context) {

    }

    default void afterEach(JesterContext context) {

    }

    default void onSuccess(JesterContext context) {

    }

    default void onDisabled(JesterContext context, Optional<String> reason) {

    }

    default void onError(JesterContext context, Throwable throwable) {

    }

    default void onServiceLaunch(JesterContext context, Service service) {

    }

    default void updateContext(JesterContext context) {

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
