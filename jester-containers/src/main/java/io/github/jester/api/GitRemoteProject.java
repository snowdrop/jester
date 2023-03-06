package io.github.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface GitRemoteProject {
    String repo();

    String branch() default "";

    String contextDir() default "";

    String[] buildCommands() default {};

    String dockerfile();

    int[] ports();

    String expectedLog() default "";

    String[] command() default {};
}
