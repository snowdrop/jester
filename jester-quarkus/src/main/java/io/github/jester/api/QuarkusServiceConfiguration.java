package io.github.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(QuarkusServiceConfigurations.class)
public @interface QuarkusServiceConfiguration {
    String forService();

    /**
     * Configure the expected log for the Quarkus service.
     */
    String expectedLog() default "Installed features";
}
