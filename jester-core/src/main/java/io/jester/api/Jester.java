package io.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import io.jester.core.JesterExtension;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(JesterExtension.class)
@Inherited
public @interface Jester {
    /**
     * Set the target environment where to run the tests. Fallback property `ts.jester.target`.
     */
    String target() default "local";

    /**
     * Enable profiling only for Java processes. Fallback property `ts.jester.enable.profiling`.
     */
    boolean enableProfiling() default false;
}
