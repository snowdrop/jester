package io.github.snowdrop.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(DockerServiceConfigurations.class)
public @interface DockerServiceConfiguration {
    String forService();

    /**
     * Configure the running container using privileged mode.
     * <p>
     * Fallback service property: "ts.services.<SERVICE NAME>.docker.privileged-mode".
     */
    boolean privileged() default false;
}
