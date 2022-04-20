package io.jcloud.resources.quarkus.local;

import static io.jcloud.utils.QuarkusUtils.QUARKUS_HTTP_PORT_PROPERTY;
import static io.jcloud.utils.QuarkusUtils.QUARKUS_SSL_PORT_PROPERTY;

import java.nio.file.Path;

import io.jcloud.api.Dependency;
import io.jcloud.core.ServiceContext;
import io.jcloud.resources.local.JavaProcessManagedResource;
import io.jcloud.resources.quarkus.common.BootstrapQuarkusResource;

public class ProdModeBootstrapQuarkusManagedResourceJava extends JavaProcessManagedResource {

    private final String location;
    private final Class<?>[] classes;
    private final Dependency[] forcedDependencies;

    private BootstrapQuarkusResource resource;

    public ProdModeBootstrapQuarkusManagedResourceJava(String location, Class<?>[] classes,
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
    protected String getSslPortProperty() {
        return QUARKUS_SSL_PORT_PROPERTY;
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
        resource = new BootstrapQuarkusResource(context, location, classes, forcedDependencies);
    }

    @Override
    protected boolean enableSsl() {
        return getAllComputedProperties().keySet().stream().anyMatch(p -> p.contains("quarkus.http.ssl"));
    }
}
