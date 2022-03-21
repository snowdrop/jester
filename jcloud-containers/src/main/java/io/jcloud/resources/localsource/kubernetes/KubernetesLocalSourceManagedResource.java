package io.jcloud.resources.localsource.kubernetes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import io.jcloud.core.ServiceContext;
import io.jcloud.resources.kubernetes.KubernetesManagedResource;
import io.jcloud.resources.localsource.LocalSourceResource;
import io.jcloud.utils.DockerUtils;
import io.jcloud.utils.PropertiesUtils;

public class KubernetesLocalSourceManagedResource extends KubernetesManagedResource {

    private final String location;
    private final String[] buildCommands;
    private final String dockerfile;
    private final String expectedLog;
    private final String[] command;
    private final Integer[] ports;

    private LocalSourceResource resource;

    public KubernetesLocalSourceManagedResource(String location, String[] buildCommands, String dockerfile,
            String expectedLog, String[] command, int[] ports) {
        this.location = PropertiesUtils.resolveProperty(location);
        this.buildCommands = PropertiesUtils.resolveProperties(buildCommands);
        this.dockerfile = PropertiesUtils.resolveProperty(dockerfile);
        this.command = PropertiesUtils.resolveProperties(command);
        this.expectedLog = PropertiesUtils.resolveProperty(expectedLog);
        this.ports = Arrays.stream(ports).boxed().toArray(Integer[]::new);
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
    protected void init(ServiceContext context) {
        super.init(context);
        this.resource = new LocalSourceResource(context, location, buildCommands, dockerfile);
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
    protected Integer[] getPorts() {
        return ports;
    }
}
