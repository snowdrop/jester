package io.jcloud.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoDbContainer {
    String image() default "quay.io/jcarvaja/mongodb:5.0";

    int[] ports() default 27017;

    String jdbcName() default "mongodb";

    String expectedLog() default "Waiting for connections";

    String[] command() default {};
}
