package io.jester.resources.spring.local;

import static io.jester.utils.SpringUtils.SERVER_HTTP_PORT;

import java.nio.file.Path;

import io.jester.core.ServiceContext;
import io.jester.resources.local.JavaProcessManagedResource;
import io.jester.resources.spring.common.SpringResource;

public class LocalBootstrapSpringManagedResourceJava extends JavaProcessManagedResource {

    private final String location;
    private final boolean forceBuild;
    private final String[] buildCommands;

    private SpringResource resource;

    public LocalBootstrapSpringManagedResourceJava(String location, boolean forceBuild, String[] buildCommands) {
        this.location = location;
        this.forceBuild = forceBuild;
        this.buildCommands = buildCommands;
    }

    @Override
    public String getDisplayName() {
        return resource.getDisplayName();
    }

    @Override
    protected String getHttpPortProperty() {
        return SERVER_HTTP_PORT;
    }

    @Override
    protected Path getRunner() {
        return resource.getRunner();
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() && resource.isRunning(getLoggingHandler());
    }

    @Override
    public boolean isFailed() {
        return super.isFailed() || resource.isFailed(getLoggingHandler());
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        resource = new SpringResource(context, location, forceBuild, buildCommands);
    }
}
