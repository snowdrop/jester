package io.github.jester.resources.localproject.local;

import java.nio.file.Files;
import java.nio.file.Path;

import io.github.jester.core.ServiceContext;
import io.github.jester.resources.common.local.GenericContainerManagedResource;
import io.github.jester.resources.localproject.LocalProjectResource;
import io.github.jester.utils.PropertiesUtils;

public class DockerLocalProjectManagedResource extends GenericContainerManagedResource {

    private final String location;
    private final String[] buildCommands;
    private final String dockerfile;

    private LocalProjectResource resource;

    public DockerLocalProjectManagedResource(String location, String[] buildCommands, String dockerfile,
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
            throw new RuntimeException("Error creating the LocalProject service " + context.getName() + ". Location '"
                    + location + "' does not exist.");
        }
    }

    @Override
    public String getDisplayName() {
        return "Local project from " + location;
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        this.resource = new LocalProjectResource(context, location, buildCommands, dockerfile);
    }

    @Override
    protected String getImage() {
        return resource.getGeneratedImage();
    }
}
