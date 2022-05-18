package io.jcloud.resources.spring.kubernetes;

import static io.jcloud.utils.SpringUtils.HTTP_PORT_DEFAULT;
import static io.jcloud.utils.SpringUtils.SERVER_HTTP_PORT;

import io.jcloud.core.ServiceContext;
import io.jcloud.resources.kubernetes.KubernetesManagedResource;
import io.jcloud.resources.spring.common.SpringResource;
import io.jcloud.utils.DockerUtils;

public class ContainerRegistrySpringManagedResource extends KubernetesManagedResource {

    private final String location;
    private final boolean forceBuild;
    private final String[] buildCommands;

    private SpringResource resource;
    private String image;

    public ContainerRegistrySpringManagedResource(String location, boolean forceBuild, String[] buildCommands) {
        this.location = location;
        this.forceBuild = forceBuild;
        this.buildCommands = buildCommands;
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
        return new int[] {
                context.getOwner().getProperty(SERVER_HTTP_PORT).map(Integer::parseInt).orElse(HTTP_PORT_DEFAULT) };
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);

        resource = new SpringResource(context, location, forceBuild, buildCommands);
        image = createImageAndPush();
    }

    private String createImageAndPush() {
        return DockerUtils.createImageAndPush(context, resource.getRunner());
    }

}
