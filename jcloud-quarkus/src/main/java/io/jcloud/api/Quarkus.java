package io.jcloud.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Quarkus {
    // By default, it will load all the classes in the classpath.
    Class<?>[] classes() default {};

    /**
     * Add forced dependencies.
     */
    Dependency[] dependencies() default {};
}
