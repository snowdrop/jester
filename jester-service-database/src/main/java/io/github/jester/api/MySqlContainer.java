package io.github.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MySqlContainer {
    String image() default "quay.io/jcarvaja/mysql:8.0";

    int[] ports() default 3306;

    String jdbcName() default "mysql";

    String expectedLog() default "ready for connections";

    String[] command() default {};

    String userProperty() default "MYSQL_USER";

    String passwordProperty() default "MYSQL_PASSWORD";

    String databaseNameProperty() default "MYSQL_DATABASE";
}
