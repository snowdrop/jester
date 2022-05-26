package io.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(SpringServiceConfigurations.class)
public @interface SpringServiceConfiguration {
    String forService();

    /**
     * Configure the expected log for the Spring service.
     */
    String expectedLog() default "Started .* in .* seconds";
}
