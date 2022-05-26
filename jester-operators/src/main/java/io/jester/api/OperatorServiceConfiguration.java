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
@Repeatable(OperatorServiceConfigurations.class)
public @interface OperatorServiceConfiguration {
    String forService();

    /**
     * Default startup timeout for services is 5 minutes. Fallback service property: "ts.services.<SERVICE
     * NAME>.operator.install.timeout".
     */
    String installTimeout() default "";
}
