package io.jester.resources.containers.database;

import java.lang.annotation.Annotation;

import io.jester.api.DatabaseService;
import io.jester.api.MySqlContainer;
import io.jester.api.Service;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.resources.containers.ContainerAnnotationBinding;

public class MySqlContainerAnnotationBinding extends ContainerAnnotationBinding {

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, MySqlContainer.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
        MySqlContainer metadata = findAnnotation(annotations, MySqlContainer.class).get();

        if (!(service instanceof DatabaseService)) {
            throw new IllegalStateException("@MySqlContainer can only be used with DatabaseService service");
        }

        DatabaseService databaseService = (DatabaseService) service;
        databaseService.withJdbcName(metadata.jdbcName());
        databaseService.withDatabaseNameProperty(metadata.databaseNameProperty());
        databaseService.withUserProperty(metadata.userProperty());
        databaseService.withPasswordProperty(metadata.passwordProperty());
        // This property is necessary because we're not setting MYSQL_ROOT_PASSWORD
        databaseService.withProperty("ALLOW_EMPTY_PASSWORD", "yes");

        return doInit(context, metadata.image(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }
}
