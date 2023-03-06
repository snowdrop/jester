package io.github.jester.resources.spring.openshift;

import io.github.jester.core.ServiceContext;
import io.github.jester.resources.openshift.OpenShiftManagedResource;
import io.github.jester.resources.spring.common.SpringResource;
import io.github.jester.utils.DockerUtils;
import io.github.jester.utils.SpringUtils;

public class ContainerRegistrySpringOpenShiftManagedResource extends OpenShiftManagedResource {

    private final String location;
    private final boolean forceBuild;
    private final String[] buildCommands;

    private SpringResource resource;
    private String image;

    public ContainerRegistrySpringOpenShiftManagedResource(String location, boolean forceBuild,
            String[] buildCommands) {
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
        return new int[] { context.getOwner().getProperty(SpringUtils.SERVER_HTTP_PORT).map(Integer::parseInt)
                .orElse(SpringUtils.HTTP_PORT_DEFAULT) };
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
