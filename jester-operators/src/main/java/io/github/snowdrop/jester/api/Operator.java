package io.github.snowdrop.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Operators.class)
@Inherited
public @interface Operator {
    String subscription();

    String channel() default "stable";

    String source() default "operatorhubio-catalog";

    String sourceNamespace() default "olm";
}
