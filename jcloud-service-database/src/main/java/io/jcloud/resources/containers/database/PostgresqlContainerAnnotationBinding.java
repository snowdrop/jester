package io.jcloud.resources.containers.database;

import java.lang.annotation.Annotation;

import io.jcloud.api.DatabaseService;
import io.jcloud.api.PostgresqlContainer;
import io.jcloud.api.Service;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;
import io.jcloud.resources.containers.ContainerAnnotationBinding;

public class PostgresqlContainerAnnotationBinding extends ContainerAnnotationBinding {

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, PostgresqlContainer.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JCloudContext context, Service service, Annotation... annotations) {
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
