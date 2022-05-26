package io.jester.resources.containers.database;

import java.lang.annotation.Annotation;

import io.jester.api.DatabaseService;
import io.jester.api.Service;
import io.jester.api.SqlServerContainer;
import io.jester.core.JesterContext;
import io.jester.core.ManagedResource;
import io.jester.resources.containers.ContainerAnnotationBinding;

public class SqlServerContainerAnnotationBinding extends ContainerAnnotationBinding {

    private static final String USER = "sa";
    private static final String PASSWORD = "My1337p@ssworD";
    private static final String DATABASE = "msdb";
    private static final String JDBC_URL_PATTERN = "jdbc:${JDBC_NAME}://${HOST}:${PORT};databaseName=${DATABASE}";
    private static final String REACTIVE_URL_PATTERN = "${JDBC_NAME}://${HOST}:${PORT};databaseName=${DATABASE}";

    @Override
    public boolean isFor(Annotation... annotations) {
        return findAnnotation(annotations, SqlServerContainer.class).isPresent();
    }

    @Override
    public ManagedResource getManagedResource(JesterContext context, Service service, Annotation... annotations) {
        SqlServerContainer metadata = findAnnotation(annotations, SqlServerContainer.class).get();

        if (!(service instanceof DatabaseService)) {
            throw new IllegalStateException("@SqlServerContainer can only be used with DatabaseService service");
        }

        DatabaseService databaseService = (DatabaseService) service;
        databaseService.withJdbcName(metadata.jdbcName());
        databaseService.withPasswordProperty(metadata.passwordProperty());
        databaseService.withJdbcUrlPattern(JDBC_URL_PATTERN);
        databaseService.withReactiveUrlPattern(REACTIVE_URL_PATTERN);
        databaseService.overrideDefaults(USER, PASSWORD, DATABASE);
        // We need to accept the SQL Server End-User License Agreement (EULA):
        databaseService.withProperty("ACCEPT_EULA", "Y");

        return doInit(context, metadata.image(), metadata.expectedLog(), metadata.command(), metadata.ports());
    }
}
