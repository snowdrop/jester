package io.github.snowdrop.jester.api;

import io.github.snowdrop.jester.core.ServiceContext;

public interface ServiceListener {

    default void onServiceError(ServiceContext service, Throwable throwable) {

    }

    default void onServiceStarted(ServiceContext service) {

    }

    default void onServiceStopped(ServiceContext service) {

    }
}
