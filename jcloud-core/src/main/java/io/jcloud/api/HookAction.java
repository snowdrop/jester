package io.jcloud.api;

@FunctionalInterface
public interface HookAction {
    void handle(Service service);
}
