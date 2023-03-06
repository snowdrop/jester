package io.github.snowdrop.jester.api;

@FunctionalInterface
public interface HookAction {
    void handle(Service service);
}
