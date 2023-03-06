package io.github.jester.api.extensions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.github.jester.api.Service;
import io.github.jester.core.DependencyContext;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ServiceContext;

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
