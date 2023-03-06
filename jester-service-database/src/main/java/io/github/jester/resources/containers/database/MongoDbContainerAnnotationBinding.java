package io.github.jester.resources.containers.database;

import java.lang.annotation.Annotation;

import io.github.jester.api.DatabaseService;
import io.github.jester.api.MongoDbContainer;
import io.github.jester.api.Service;
import io.github.jester.core.JesterContext;
import io.github.jester.core.ManagedResource;
import io.github.jester.resources.containers.ContainerAnnotationBinding;

public class MongoDbContainerAnnotationBinding extends ContainerAnnotationBinding {

    private static final String URL_PATTERN = "${JDBC_NAME}://${HOST}:${PORT}";

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, MongoDbContainer.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
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
