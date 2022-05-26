package io.jester.resources.gitremoteproject;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import io.jester.api.GitRemoteProject;
import io.jester.api.Service;
import io.jester.api.extensions.AnnotationBinding;
import io.jester.api.extensions.GitRemoteProjectManagedResourceBinding;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.resources.gitremoteproject.local.DockerGitRemoteProjectManagedResource;

public class GitRemoteProjectAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<GitRemoteProjectManagedResourceBinding> bindings = ServiceLoader
            .load(GitRemoteProjectManagedResourceBinding.class);

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, GitRemoteProject.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
        GitRemoteProject metadata = findAnnotation(annotations, GitRemoteProject.class).get();

        for (GitRemoteProjectManagedResourceBinding binding : bindings) {
            if (binding.appliesFor(context)) {
                return binding.init(metadata);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new DockerGitRemoteProjectManagedResource(metadata.repo(), metadata.branch(), metadata.contextDir(),
                metadata.buildCommands(), metadata.dockerfile(), metadata.expectedLog(), metadata.command(),
                metadata.ports());
    }

}
