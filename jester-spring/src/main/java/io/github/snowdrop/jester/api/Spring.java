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
public @interface Spring {

    /**
     * Specify the path location where the Spring Boot application module is located. By default, it will use the
     * current module.
     */
    String location() default ".";

    /**
     * Force build the JAR application.
     */
    boolean forceBuild() default false;

    /**
     * This is only used when the jar file is not found (the test is executed directly from the IDE) or forceBuild is
     * enabled.
     */
    String[] buildCommands() default { "mvn", "package" };
}
