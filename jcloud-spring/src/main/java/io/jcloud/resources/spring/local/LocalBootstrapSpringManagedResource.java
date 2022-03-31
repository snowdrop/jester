package io.jcloud.resources.spring.local;

import static io.jcloud.utils.SpringUtils.APPLICATION_PROPERTIES;
import static io.jcloud.utils.SpringUtils.SERVER_HTTP_PORT;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import io.jcloud.core.ServiceContext;
import io.jcloud.resources.local.ProcessManagedResource;
import io.jcloud.resources.spring.common.SpringResource;
import io.jcloud.utils.PropertiesUtils;

public class LocalBootstrapSpringManagedResource extends ProcessManagedResource {

    private final boolean forceBuild;
    private final String[] buildCommands;

    private SpringResource resource;

    public LocalBootstrapSpringManagedResource(boolean forceBuild, String[] buildCommands) {
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
    public String getProperty(String name) {
        Path applicationProperties = getComputedApplicationProperties();
        if (!Files.exists(applicationProperties)) {
            return null;
        }

        Map<String, String> computedProperties = PropertiesUtils.toMap(applicationProperties);
        return Optional.ofNullable(computedProperties.get(name))
                .orElseGet(() -> computedProperties.get(propertyWithProfile(name)));
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        resource = new SpringResource(context, forceBuild, buildCommands);
    }

    private Path getComputedApplicationProperties() {
        return context.getServiceFolder().resolve(APPLICATION_PROPERTIES);
    }

    private String propertyWithProfile(String name) {
        return "%" + context.getScenarioContext().getRunningTestClassName() + "." + name;
    }
}
