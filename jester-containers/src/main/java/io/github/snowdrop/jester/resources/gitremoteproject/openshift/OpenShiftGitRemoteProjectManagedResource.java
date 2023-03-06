package io.github.snowdrop.jester.resources.gitremoteproject.openshift;

import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.resources.gitremoteproject.GitRemoteProjectResource;
import io.github.snowdrop.jester.resources.openshift.OpenShiftManagedResource;
import io.github.snowdrop.jester.utils.DockerUtils;
import io.github.snowdrop.jester.utils.PropertiesUtils;

public class OpenShiftGitRemoteProjectManagedResource extends OpenShiftManagedResource {

    private final String repo;
    private final String branch;
    private final String contextDir;
    private final String[] buildCommands;
    private final String dockerfile;
    private final String expectedLog;
    private final String[] command;
    private final int[] ports;

    private GitRemoteProjectResource resource;

    public OpenShiftGitRemoteProjectManagedResource(String repo, String branch, String contextDir,
            String[] buildCommands, String dockerfile, String expectedLog, String[] command, int[] ports) {
        this.repo = PropertiesUtils.resolveProperty(repo);
        this.branch = PropertiesUtils.resolveProperty(branch);
        this.contextDir = PropertiesUtils.resolveProperty(contextDir);
        this.buildCommands = PropertiesUtils.resolveProperties(buildCommands);
        this.dockerfile = PropertiesUtils.resolveProperty(dockerfile);
        this.command = PropertiesUtils.resolveProperties(command);
        this.expectedLog = PropertiesUtils.resolveProperty(expectedLog);
        this.ports = ports;
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
    protected int[] getPorts() {
        return ports;
    }
}
