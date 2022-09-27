package io.jester.resources.quarkus.local;

import static io.jester.utils.Ports.DEFAULT_SSL_PORT;
import static io.jester.utils.QuarkusUtils.QUARKUS_GRPC_SERVER_PORT;
import static io.jester.utils.QuarkusUtils.QUARKUS_HTTP_PORT_PROPERTY;
import static io.jester.utils.QuarkusUtils.QUARKUS_SSL_PORT_PROPERTY;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import io.jester.api.Dependency;
import io.jester.core.ServiceContext;
import io.jester.resources.local.JavaProcessManagedResource;
import io.jester.resources.quarkus.common.BootstrapQuarkusResource;
import io.jester.utils.Ports;
import io.jester.utils.SocketUtils;

public class ProdModeBootstrapQuarkusManagedResourceJava extends JavaProcessManagedResource {

    private final String location;
    private final String[] buildCommands;
    private final Class<?>[] classes;
    private final Dependency[] forcedDependencies;
    private final boolean forceBuild;

    private BootstrapQuarkusResource resource;

    public ProdModeBootstrapQuarkusManagedResourceJava(String location, String[] buildCommands, Class<?>[] classes,
            Dependency[] forcedDependencies, boolean forceBuild) {
        this.location = location;
        this.buildCommands = buildCommands;
        this.classes = classes;
        this.forcedDependencies = forcedDependencies;
        this.forceBuild = forceBuild;
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
        resource = new BootstrapQuarkusResource(context, location, buildCommands, classes, forcedDependencies,
                forceBuild);
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
