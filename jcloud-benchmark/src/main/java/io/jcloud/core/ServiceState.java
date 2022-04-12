package io.jcloud.core;

import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import io.jcloud.api.Service;

@State(Scope.Benchmark)
public class ServiceState<T extends Service> {

    private final T service;
    private final Service[] dependencies;

    public ServiceState(T service, Service... dependencies) {
        this.service = service;
        this.dependencies = dependencies;
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
        Stream.of(dependencies).forEach(Service::start);
        service.start();
    }

    @TearDown(Level.Trial)
    public void doTearDown() {
        service.stop();
        Stream.of(dependencies).forEach(Service::stop);
    }
}
