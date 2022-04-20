package io.jcloud.resources.spring.local;

import static io.jcloud.utils.SpringUtils.SERVER_HTTP_PORT;

import java.nio.file.Path;

import io.jcloud.core.ServiceContext;
import io.jcloud.resources.local.JavaProcessManagedResource;
import io.jcloud.resources.spring.common.SpringResource;

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

    @Override
    protected boolean enableSsl() {
        return getAllComputedProperties().keySet().stream().anyMatch(p -> p.contains("server.ssl"));
    }
}
