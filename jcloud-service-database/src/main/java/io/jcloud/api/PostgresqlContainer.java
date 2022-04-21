package io.jcloud.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PostgresqlContainer {
    String image() default "quay.io/jcarvaja/postgresql:13.5.0";

    int[] ports() default 5432;

    String jdbcName() default "postgresql";

    String expectedLog() default "listening on IPv4 address";

    String[] command() default {};

    String userProperty() default "POSTGRESQL_USER";

    String passwordProperty() default "POSTGRESQL_PASSWORD";

    String databaseNameProperty() default "POSTGRESQL_DATABASE";
}
