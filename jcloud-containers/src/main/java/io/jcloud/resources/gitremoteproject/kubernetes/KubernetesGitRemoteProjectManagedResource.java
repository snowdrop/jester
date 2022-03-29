package io.jcloud.resources.gitremoteproject.kubernetes;

import java.util.Arrays;

import io.jcloud.core.ServiceContext;
import io.jcloud.resources.gitremoteproject.GitRemoteProjectResource;
import io.jcloud.resources.kubernetes.KubernetesManagedResource;
import io.jcloud.utils.DockerUtils;
import io.jcloud.utils.PropertiesUtils;

public class KubernetesGitRemoteProjectManagedResource extends KubernetesManagedResource {

    private final String repo;
    private final String branch;
    private final String contextDir;
    private final String[] buildCommands;
    private final String dockerfile;
    private final String expectedLog;
    private final String[] command;
    private final Integer[] ports;

    private GitRemoteProjectResource resource;

    public KubernetesGitRemoteProjectManagedResource(String repo, String branch, String contextDir,
            String[] buildCommands, String dockerfile, String expectedLog, String[] command, int[] ports) {
        this.repo = PropertiesUtils.resolveProperty(repo);
        this.branch = PropertiesUtils.resolveProperty(branch);
        this.contextDir = PropertiesUtils.resolveProperty(contextDir);
        this.buildCommands = PropertiesUtils.resolveProperties(buildCommands);
        this.dockerfile = PropertiesUtils.resolveProperty(dockerfile);
        this.command = PropertiesUtils.resolveProperties(command);
        this.expectedLog = PropertiesUtils.resolveProperty(expectedLog);
        this.ports = Arrays.stream(ports).boxed().toArray(Integer[]::new);
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        this.resource = new GitRemoteProjectResource(context, repo, branch, contextDir, buildCommands, dockerfile);
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
