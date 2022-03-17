package io.jcloud.resources.spring.kubernetes;

import static io.jcloud.utils.SpringUtils.HTTP_PORT_DEFAULT;
import static io.jcloud.utils.SpringUtils.SERVER_HTTP_PORT;

import io.jcloud.core.ServiceContext;
import io.jcloud.resources.kubernetes.KubernetesManagedResource;
import io.jcloud.resources.spring.common.SpringResource;
import io.jcloud.utils.DockerUtils;

public class ContainerRegistrySpringManagedResource extends KubernetesManagedResource {

    private SpringResource resource;
    private String image;

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
        return new Integer[] {
                context.getOwner().getProperty(SERVER_HTTP_PORT).map(Integer::parseInt).orElse(HTTP_PORT_DEFAULT) };
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);

        resource = new SpringResource(context);
        image = createImageAndPush();
    }

    private String createImageAndPush() {
        return DockerUtils.createImageAndPush(context, resource.getRunner());
    }

}
