package io.jcloud.resources.containers.database;

import java.lang.annotation.Annotation;

import io.jcloud.api.DatabaseService;
import io.jcloud.api.MySqlContainer;
import io.jcloud.api.Service;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;
import io.jcloud.resources.containers.ContainerAnnotationBinding;

public class MySqlContainerAnnotationBinding extends ContainerAnnotationBinding {

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, MySqlContainer.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JCloudContext context, Service service, Annotation... annotations) {
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
