package io.jcloud.resources.containers.local;

import io.jcloud.resources.common.local.GenericContainerManagedResource;
import io.jcloud.utils.PropertiesUtils;

public class DockerContainerManagedResource extends GenericContainerManagedResource {

    private final String image;

    public DockerContainerManagedResource(String image, String expectedLog, String[] command, int[] ports) {
        super(expectedLog, command, ports);
        this.image = PropertiesUtils.resolveProperty(image);
    }

    @Override
    public String getDisplayName() {
        return image;
    }

    @Override
    protected String getImage() {
        return image;
    }
}
