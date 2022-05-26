package io.jester.api;

@FunctionalInterface
public interface HookAction {
    void handle(Service service);
}
