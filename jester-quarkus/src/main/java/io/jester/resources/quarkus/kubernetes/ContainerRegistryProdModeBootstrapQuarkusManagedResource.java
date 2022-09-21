package io.jester.resources.quarkus.kubernetes;

import static io.jester.utils.QuarkusUtils.HTTP_PORT_DEFAULT;
import static io.jester.utils.QuarkusUtils.QUARKUS_GRPC_SERVER_PORT;
import static io.jester.utils.QuarkusUtils.QUARKUS_HTTP_PORT_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.jester.api.Dependency;
import io.jester.core.ServiceContext;
import io.jester.resources.kubernetes.KubernetesManagedResource;
import io.jester.resources.quarkus.common.BootstrapQuarkusResource;
import io.jester.utils.DockerUtils;
import io.jester.utils.QuarkusUtils;

public class ContainerRegistryProdModeBootstrapQuarkusManagedResource extends KubernetesManagedResource {

    private final String location;
    private final Class<?>[] classes;
    private final Dependency[] forcedDependencies;
    private final boolean forceBuild;

    private BootstrapQuarkusResource resource;
    private String image;

    public ContainerRegistryProdModeBootstrapQuarkusManagedResource(String location, Class<?>[] classes,
            Dependency[] forcedDependencies, boolean forceBuild) {
        this.location = location;
        this.classes = classes;
        this.forcedDependencies = forcedDependencies;
        this.forceBuild = forceBuild;
    }

    @Override
    public String getDisplayName() {
        return resource.getDisplayName();
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    protected String getExpectedLog() {
        return resource.getExpectedLog();
    }

    @Override
    protected int[] getPorts() {
        List<Integer> ports = new ArrayList<>();
        ports.add(Optional.ofNullable(getProperty(QUARKUS_HTTP_PORT_PROPERTY)).map(Integer::parseInt)
                .orElse(HTTP_PORT_DEFAULT));

        Optional.ofNullable(getProperty(QUARKUS_GRPC_SERVER_PORT)).map(Integer::parseInt)
                .ifPresent(grpcPort -> ports.add(grpcPort));

        return ports.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);

        resource = new BootstrapQuarkusResource(context, location, classes, forcedDependencies, forceBuild);
        image = createImageAndPush();
    }

    private String createImageAndPush() {
        String dockerFile = QuarkusUtils.getDockerfile(resource.getLaunchMode());
        return DockerUtils.createImageAndPush(context, dockerFile, resource.getRunner());
    }

}
