package io.github.snowdrop.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import io.github.snowdrop.jester.core.JesterExtension;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(JesterExtension.class)
@Inherited
public @interface Quarkus {

    /**
     * Specify the path location where the Quarkus application module is located. By default, it will use the current
     * module.
     */
    String location() default ".";

    /**
     * By default, it will load all the classes in the classpath.
     */
    Class<?>[] classes() default {};

    /**
     * Add forced dependencies.
     */
    Dependency[] dependencies() default {};

    /**
     * @return if force a custom build when running the test. If enabled, it won't use the built JAR or Native binary.
     */
    boolean forceBuild() default false;

    /**
     * @return concrete Quarkus version to build the Quarkus app. If sets, it will force the app to be built at test
     *         time.
     */
    String version() default "";
}
