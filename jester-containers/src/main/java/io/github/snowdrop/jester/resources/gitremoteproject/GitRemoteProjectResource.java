package io.github.snowdrop.jester.resources.gitremoteproject;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.resources.localproject.LocalProjectResource;
import io.github.snowdrop.jester.utils.GitUtils;

public class GitRemoteProjectResource extends LocalProjectResource {

    public GitRemoteProjectResource(ServiceContext context, String repo, String branch, String contextDir,
            String[] buildCommands, String dockerfile) {
        super(context, downloadProject(context, repo, branch, contextDir), buildCommands,
                resolveLocation(context, dockerfile));
    }

    private static String downloadProject(ServiceContext context, String repo, String branch, String contextDir) {
        GitUtils.cloneRepository(context, repo);
        if (StringUtils.isNotEmpty(branch)) {
            GitUtils.checkoutBranch(context, branch);
        }

        Path location = context.getServiceFolder();
        if (StringUtils.isNotEmpty(contextDir)) {
            location = location.resolve(contextDir);
        }

        return location.toString();
    }

    private static String resolveLocation(ServiceContext context, String dockerfile) {
        // try current location
        if (Path.of(dockerfile).toFile().exists()) {
            return dockerfile;
        }

        // fallback to service folder location
        return context.getServiceFolder().resolve(dockerfile).toString();
    }
}
