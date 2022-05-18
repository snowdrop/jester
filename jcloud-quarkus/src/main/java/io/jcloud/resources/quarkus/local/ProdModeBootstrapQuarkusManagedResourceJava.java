package io.jcloud.resources.quarkus.local;

import static io.jcloud.utils.Ports.DEFAULT_SSL_PORT;
import static io.jcloud.utils.QuarkusUtils.QUARKUS_GRPC_SERVER_PORT;
import static io.jcloud.utils.QuarkusUtils.QUARKUS_HTTP_PORT_PROPERTY;
import static io.jcloud.utils.QuarkusUtils.QUARKUS_SSL_PORT_PROPERTY;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import io.jcloud.api.Dependency;
import io.jcloud.core.ServiceContext;
import io.jcloud.resources.local.JavaProcessManagedResource;
import io.jcloud.resources.quarkus.common.BootstrapQuarkusResource;
import io.jcloud.utils.Ports;
import io.jcloud.utils.SocketUtils;

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
    protected Map<Integer, Integer> assignCustomPorts() {
        Map<Integer, Integer> customPorts = new HashMap<>();

        // ssl port
        if (isSslEnabled()) {
            int assignedSslPort = getOrAssignPortByProperty(QUARKUS_SSL_PORT_PROPERTY);
            Ports.SSL_PORTS.forEach(sslPort -> customPorts.put(DEFAULT_SSL_PORT, assignedSslPort));
            propertiesToOverwrite.put(QUARKUS_SSL_PORT_PROPERTY, "" + assignedSslPort);
        }

        // grpc port
        String grpcPort = getProperty(QUARKUS_GRPC_SERVER_PORT);
        if (grpcPort != null) {
            int assignedGrpcPort = SocketUtils.findAvailablePort(context.getOwner());
            customPorts.put(Integer.parseInt(grpcPort), assignedGrpcPort);
            propertiesToOverwrite.put(QUARKUS_GRPC_SERVER_PORT, "" + assignedGrpcPort);
        }

        return customPorts;
    }

    private boolean isSslEnabled() {
        return getAllComputedProperties().keySet().stream().anyMatch(p -> p.contains("quarkus.http.ssl"));
    }
}
