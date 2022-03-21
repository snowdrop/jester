package io.jcloud.resources.localsource.local;

import java.nio.file.Files;
import java.nio.file.Path;

import io.jcloud.core.ServiceContext;
import io.jcloud.resources.common.local.GenericContainerManagedResource;
import io.jcloud.resources.localsource.LocalSourceResource;
import io.jcloud.utils.PropertiesUtils;

public class DockerLocalSourceManagedResource extends GenericContainerManagedResource {

    private final String location;
    private final String[] buildCommands;
    private final String dockerfile;

    private LocalSourceResource resource;

    public DockerLocalSourceManagedResource(String location, String[] buildCommands, String dockerfile,
            String expectedLog, String[] command, int[] ports) {
        super(expectedLog, command, ports);
        this.location = PropertiesUtils.resolveProperty(location);
        this.buildCommands = PropertiesUtils.resolveProperties(buildCommands);
        this.dockerfile = PropertiesUtils.resolveProperty(dockerfile);
    }

    @Override
    public void validate() {
        super.validate();
        if (!Files.exists(Path.of(location))) {
            throw new RuntimeException("Error creating the LocalSource service " + context.getName() + ". Location '"
                    + location + "' does not exist.");
        }
    }

    @Override
    public String getDisplayName() {
        return "Local Source " + location;
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        this.resource = new LocalSourceResource(context, location, buildCommands, dockerfile);
    }

    @Override
    protected String getImage() {
        return resource.getGeneratedImage();
    }
}
