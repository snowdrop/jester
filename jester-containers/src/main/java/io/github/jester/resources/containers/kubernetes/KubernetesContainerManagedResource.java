package io.github.jester.resources.containers.kubernetes;

import io.github.jester.resources.kubernetes.KubernetesManagedResource;
import io.github.jester.utils.PropertiesUtils;

public class KubernetesContainerManagedResource extends KubernetesManagedResource {

    private final String image;
    private final String expectedLog;
    private final String[] command;
    private final int[] ports;

    public KubernetesContainerManagedResource(String image, String expectedLog, String[] command, int[] ports) {
        this.image = PropertiesUtils.resolveProperty(image);
        this.command = PropertiesUtils.resolveProperties(command);
        this.expectedLog = PropertiesUtils.resolveProperty(expectedLog);
        this.ports = ports;
    }

    @Override
    protected String getImage() {
        return image;
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
