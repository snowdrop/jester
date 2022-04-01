package io.jcloud.resources.quarkus.local;

import static io.jcloud.utils.QuarkusUtils.APPLICATION_PROPERTIES;
import static io.jcloud.utils.QuarkusUtils.QUARKUS_HTTP_PORT_PROPERTY;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import io.jcloud.api.Dependency;
import io.jcloud.core.ServiceContext;
import io.jcloud.resources.local.ProcessManagedResource;
import io.jcloud.resources.quarkus.common.BootstrapQuarkusResource;
import io.jcloud.utils.PropertiesUtils;

public class ProdModeBootstrapQuarkusManagedResource extends ProcessManagedResource {

    private final String location;
    private final Class<?>[] classes;
    private final Dependency[] forcedDependencies;

    private BootstrapQuarkusResource resource;

    public ProdModeBootstrapQuarkusManagedResource(String location, Class<?>[] classes,
            Dependency[] forcedDependencies) {
        this.location = location;
        this.classes = classes;
        this.forcedDependencies = forcedDependencies;
    }

    @Override
    public String getDisplayName() {
        return resource.getDisplayName();
    }

    @Override
    protected String getHttpPortProperty() {
        return QUARKUS_HTTP_PORT_PROPERTY;
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
        resource = new BootstrapQuarkusResource(context, location, classes, forcedDependencies);
    }

    private Path getComputedApplicationProperties() {
        return context.getServiceFolder().resolve(APPLICATION_PROPERTIES);
    }

    private String propertyWithProfile(String name) {
        return "%" + context.getScenarioContext().getRunningTestClassName() + "." + name;
    }
}
