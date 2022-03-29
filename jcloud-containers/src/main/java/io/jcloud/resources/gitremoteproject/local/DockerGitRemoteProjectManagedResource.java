package io.jcloud.resources.gitremoteproject.local;

import io.jcloud.core.ServiceContext;
import io.jcloud.resources.common.local.GenericContainerManagedResource;
import io.jcloud.resources.gitremoteproject.GitRemoteProjectResource;
import io.jcloud.utils.PropertiesUtils;

public class DockerGitRemoteProjectManagedResource extends GenericContainerManagedResource {

    private final String repo;
    private final String branch;
    private final String contextDir;
    private final String[] buildCommands;
    private final String dockerfile;

    private GitRemoteProjectResource resource;

    public DockerGitRemoteProjectManagedResource(String repo, String branch, String contextDir, String[] buildCommands,
            String dockerfile, String expectedLog, String[] command, int[] ports) {
        super(expectedLog, command, ports);
        this.repo = PropertiesUtils.resolveProperty(repo);
        this.branch = PropertiesUtils.resolveProperty(branch);
        this.contextDir = PropertiesUtils.resolveProperty(contextDir);
        this.buildCommands = PropertiesUtils.resolveProperties(buildCommands);
        this.dockerfile = PropertiesUtils.resolveProperty(dockerfile);
    }

    @Override
    public String getDisplayName() {
        return "Git project from " + repo;
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        this.resource = new GitRemoteProjectResource(context, repo, branch, contextDir, buildCommands, dockerfile);
    }

    @Override
    protected String getImage() {
        return resource.getGeneratedImage();
    }
}
