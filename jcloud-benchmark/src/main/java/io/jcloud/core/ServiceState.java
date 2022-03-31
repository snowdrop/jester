package io.jcloud.core;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import io.jcloud.api.Service;

@State(Scope.Benchmark)
public class ServiceState<T extends Service> {

    private final T service;

    public ServiceState(T service) {
        this.service = service;
        if (service.isRunning()) {
            throw new IllegalStateException(
                    "Service is already running! You need to declare services using `.setAutoStart(false)`");
        }
    }

    public T getService() {
        return service;
    }

    @Setup(Level.Trial)
    public void doSetup() {
        service.start();
    }

    @TearDown(Level.Trial)
    public void doTearDown() {
        service.stop();
    }
}
