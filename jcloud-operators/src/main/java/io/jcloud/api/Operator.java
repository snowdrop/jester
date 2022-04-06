package io.jcloud.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Operator {
    String name();

    String channel() default "stable";

    String source() default "operatorhubio-catalog";

    String sourceNamespace() default "olm";
}
