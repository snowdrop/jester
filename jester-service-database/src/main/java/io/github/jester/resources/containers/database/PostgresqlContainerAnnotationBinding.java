package io.github.jester.resources.containers.database;

import java.lang.annotation.Annotation;

import io.github.jester.api.DatabaseService;
import io.github.jester.api.PostgresqlContainer;
import io.github.jester.api.Service;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.resources.containers.ContainerAnnotationBinding;

public class PostgresqlContainerAnnotationBinding extends ContainerAnnotationBinding {

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, PostgresqlContainer.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
        PostgresqlContainer metadata = findAnnotation(annotations, PostgresqlContainer.class).get();

        if (!(service instanceof DatabaseService)) {
            throw new IllegalStateException("@PostgresqlContainer can only be used with DatabaseService service");
        }

        DatabaseService databaseService = (DatabaseService) service;
        databaseService.withJdbcName(metadata.jdbcName());
        databaseService.withDatabaseNameProperty(metadata.databaseNameProperty());
        databaseService.withUserProperty(metadata.userProperty());
        databaseService.withPasswordProperty(metadata.passwordProperty());

        return doInit(context, metadata.image(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }
}
