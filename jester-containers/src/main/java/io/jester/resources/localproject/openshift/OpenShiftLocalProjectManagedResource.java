package io.jester.resources.localproject.openshift;

import java.nio.file.Files;
import java.nio.file.Path;

import io.jester.core.ServiceContext;
import io.jester.resources.localproject.LocalProjectResource;
import io.jester.resources.openshift.OpenShiftManagedResource;
import io.jester.utils.DockerUtils;
import io.jester.utils.PropertiesUtils;

public class OpenShiftLocalProjectManagedResource extends OpenShiftManagedResource {

    private final String location;
    private final String[] buildCommands;
    private final String dockerfile;
    private final String expectedLog;
    private final String[] command;
    private final int[] ports;

    private LocalProjectResource resource;

    public OpenShiftLocalProjectManagedResource(String location, String[] buildCommands, String dockerfile,
            String expectedLog, String[] command, int[] ports) {
        this.location = PropertiesUtils.resolveProperty(location);
        this.buildCommands = PropertiesUtils.resolveProperties(buildCommands);
        this.dockerfile = PropertiesUtils.resolveProperty(dockerfile);
        this.command = PropertiesUtils.resolveProperties(command);
        this.expectedLog = PropertiesUtils.resolveProperty(expectedLog);
        this.ports = ports;
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
    protected void init(ServiceContext context) {
        super.init(context);
        this.resource = new LocalProjectResource(context, location, buildCommands, dockerfile);
        DockerUtils.push(context);
    }

    @Override
    protected String getImage() {
        return resource.getGeneratedImage();
    }

    @Override
    protected String getExpectedLog() {
        return expectedLog;
    }

    @Override
    protected String[] getCommand() {
        return command;
    }

    @Override
    protected int[] getPorts() {
        return ports;
    }
}
