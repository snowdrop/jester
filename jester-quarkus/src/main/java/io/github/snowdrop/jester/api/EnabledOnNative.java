package io.github.snowdrop.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import io.github.snowdrop.jester.api.conditions.EnabledOnNativeCondition;

@Inherited
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(EnabledOnNativeCondition.class)
public @interface EnabledOnNative {
    /**
     * Why is the annotated test class or test method disabled.
     */
    String reason() default "";
}
