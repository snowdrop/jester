package io.jester.resources.spring.kubernetes;

import static io.jester.utils.SpringUtils.HTTP_PORT_DEFAULT;
import static io.jester.utils.SpringUtils.SERVER_HTTP_PORT;

import io.jester.core.ServiceContext;
import io.jester.resources.kubernetes.KubernetesManagedResource;
import io.jester.resources.spring.common.SpringResource;
import io.jester.utils.DockerUtils;

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
