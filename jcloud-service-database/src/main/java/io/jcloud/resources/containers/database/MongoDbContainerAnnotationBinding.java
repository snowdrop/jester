package io.jcloud.resources.containers.database;

import java.lang.annotation.Annotation;

import io.jcloud.api.DatabaseService;
import io.jcloud.api.MongoDbContainer;
import io.jcloud.api.Service;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ManagedResource;
import io.jcloud.resources.containers.ContainerAnnotationBinding;

public class MongoDbContainerAnnotationBinding extends ContainerAnnotationBinding {

    private static final String URL_PATTERN = "${JDBC_NAME}://${HOST}:${PORT}";

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, MongoDbContainer.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JCloudContext context, Service service, Annotation... annotations) {
        MongoDbContainer metadata = findAnnotation(annotations, MongoDbContainer.class).get();

        if (!(service instanceof DatabaseService)) {
            throw new IllegalStateException("@MongoDbContainer can only be used with DatabaseService service");
        }

        DatabaseService databaseService = (DatabaseService) service;
        databaseService.withJdbcName(metadata.jdbcName());
        databaseService.withJdbcUrlPattern(URL_PATTERN);
        databaseService.withReactiveUrlPattern(URL_PATTERN);

        return doInit(context, metadata.image(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }
}
