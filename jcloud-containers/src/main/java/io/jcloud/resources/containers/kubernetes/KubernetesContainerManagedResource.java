package io.jcloud.resources.containers.kubernetes;

import java.util.Arrays;

import io.jcloud.resources.kubernetes.KubernetesManagedResource;
import io.jcloud.utils.PropertiesUtils;

public class KubernetesContainerManagedResource extends KubernetesManagedResource {

    private final String image;
    private final String expectedLog;
    private final String[] command;
    private final Integer[] ports;

    public KubernetesContainerManagedResource(String image, String expectedLog, String[] command, int[] ports) {
        this.image = PropertiesUtils.resolveProperty(image);
        this.command = command;
        this.expectedLog = PropertiesUtils.resolveProperty(expectedLog);
        this.ports = Arrays.stream(ports).boxed().toArray(Integer[]::new);
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
    protected Integer[] getPorts() {
        return ports;
    }
}
