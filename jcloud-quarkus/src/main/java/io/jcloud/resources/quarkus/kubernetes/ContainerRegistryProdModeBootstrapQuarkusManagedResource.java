package io.jcloud.resources.quarkus.kubernetes;

import static io.jcloud.utils.QuarkusUtils.HTTP_PORT_DEFAULT;
import static io.jcloud.utils.QuarkusUtils.QUARKUS_HTTP_PORT_PROPERTY;

import io.jcloud.api.Dependency;
import io.jcloud.core.ServiceContext;
import io.jcloud.resources.kubernetes.KubernetesManagedResource;
import io.jcloud.resources.quarkus.common.BootstrapQuarkusResource;
import io.jcloud.utils.DockerUtils;
import io.jcloud.utils.QuarkusUtils;

public class ContainerRegistryProdModeBootstrapQuarkusManagedResource extends KubernetesManagedResource {

    private final Class<?>[] classes;
    private final Dependency[] forcedDependencies;

    private BootstrapQuarkusResource resource;
    private String image;

    public ContainerRegistryProdModeBootstrapQuarkusManagedResource(Class<?>[] classes,
            Dependency[] forcedDependencies) {
        this.classes = classes;
        this.forcedDependencies = forcedDependencies;
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
    protected Integer[] getPorts() {
        return new Integer[] { context.getOwner().getProperty(QUARKUS_HTTP_PORT_PROPERTY).map(Integer::parseInt)
                .orElse(HTTP_PORT_DEFAULT) };
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);

        resource = new BootstrapQuarkusResource(context, classes, forcedDependencies);
        image = createImageAndPush();
    }

    private String createImageAndPush() {
        String dockerFile = QuarkusUtils.getDockerfile(resource.getLaunchMode());
        return DockerUtils.createImageAndPush(context, dockerFile, resource.getRunner());
    }

}
