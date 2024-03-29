package io.github.snowdrop.jester.resources.containers.local;

import io.github.snowdrop.jester.resources.common.local.GenericContainerManagedResource;
import io.github.snowdrop.jester.utils.PropertiesUtils;

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
