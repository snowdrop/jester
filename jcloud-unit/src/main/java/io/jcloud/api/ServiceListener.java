package io.jcloud.api;

import io.jcloud.core.ServiceContext;

public interface ServiceListener {

    default void onServiceError(ServiceContext service, Throwable throwable) {

    }

    default void onServiceStarted(ServiceContext service) {

    }

    default void onServiceStopped(ServiceContext service) {

    }
}
