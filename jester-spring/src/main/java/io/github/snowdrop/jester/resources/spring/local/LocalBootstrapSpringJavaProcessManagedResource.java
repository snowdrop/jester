package io.github.snowdrop.jester.resources.spring.local;

import java.nio.file.Path;

import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.resources.local.JavaProcessManagedResource;
import io.github.snowdrop.jester.resources.spring.common.SpringResource;
import io.github.snowdrop.jester.utils.SpringUtils;

public class LocalBootstrapSpringJavaProcessManagedResource extends JavaProcessManagedResource {

    private final String location;
    private final boolean forceBuild;
    private final String[] buildCommands;

    private SpringResource resource;

    public LocalBootstrapSpringJavaProcessManagedResource(String location, boolean forceBuild, String[] buildCommands) {
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
        return SpringUtils.SERVER_HTTP_PORT;
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
