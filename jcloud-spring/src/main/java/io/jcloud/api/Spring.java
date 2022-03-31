package io.jcloud.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Spring {
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
