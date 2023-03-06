package io.github.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MariaDbContainer {
    String image() default "quay.io/jcarvaja/mariadb:10.6";

    int[] ports() default 3306;

    String jdbcName() default "mariadb";

    String expectedLog() default "ready for connections";

    String[] command() default {};

    String userProperty() default "MARIADB_USER";

    String passwordProperty() default "MARIADB_PASSWORD";

    String passwordRootProperty() default "MARIADB_ROOT_PASSWORD";

    String databaseNameProperty() default "MARIADB_DATABASE";
}
