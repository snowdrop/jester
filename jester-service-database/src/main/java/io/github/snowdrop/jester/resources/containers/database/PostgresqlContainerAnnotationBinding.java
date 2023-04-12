package io.github.snowdrop.jester.resources.containers.database;

import java.lang.annotation.Annotation;

import io.github.snowdrop.jester.api.DatabaseService;
import io.github.snowdrop.jester.api.PostgresqlContainer;
import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.resources.containers.ContainerAnnotationBinding;

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

        return doInit(context, service, metadata.image(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }
}
