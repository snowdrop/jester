package io.jester.logging;

import io.jester.api.Service;

public abstract class ServiceLoggingHandler extends LoggingHandler {

    private final Service service;

    public ServiceLoggingHandler(Service service) {
        this.service = service;
    }

    @Override
    protected void logInfo(String line) {
        Log.info(service, line);
    }

    @Override
    protected boolean isLogEnabled() {
        return service.getConfiguration().isLogEnabled();
    }

}
