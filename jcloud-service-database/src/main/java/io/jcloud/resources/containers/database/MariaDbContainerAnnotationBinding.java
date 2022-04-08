package io.jcloud.resources.containers.database;

import java.lang.annotation.Annotation;

import io.jcloud.api.DatabaseService;
import io.jcloud.api.MariaDbContainer;
import io.jcloud.api.Service;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;
import io.jcloud.resources.containers.ContainerAnnotationBinding;

public class MariaDbContainerAnnotationBinding extends ContainerAnnotationBinding {

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, MariaDbContainer.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JCloudContext context, Service service, Annotation... annotations) {
        MariaDbContainer metadata = findAnnotation(annotations, MariaDbContainer.class).get();

        if (!(service instanceof DatabaseService)) {
            throw new IllegalStateException("@MariaDbContainer can only be used with DatabaseService service");
        }

        DatabaseService databaseService = (DatabaseService) service;
        databaseService.withJdbcName(metadata.jdbcName());
        databaseService.withDatabaseNameProperty(metadata.databaseNameProperty());
        databaseService.withUserProperty(metadata.userProperty());
        databaseService.withPasswordProperty(metadata.passwordProperty());
        // This property is necessary because we're not setting MARIADB_ROOT_PASSWORD
        databaseService.withProperty("ALLOW_EMPTY_PASSWORD", "yes");

        return doInit(context, metadata.image(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }
}
