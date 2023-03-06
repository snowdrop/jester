package io.github.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlServerContainer {
    String image() default "mcr.microsoft.com/mssql/rhel/server:2019-latest";

    int[] ports() default 1433;

    String jdbcName() default "sqlserver";

    String expectedLog() default "Service Broker manager has started";

    String[] command() default {};

    String passwordProperty() default "SA_PASSWORD";
}
