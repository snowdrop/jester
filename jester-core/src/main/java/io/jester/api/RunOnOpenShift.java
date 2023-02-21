package io.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RunOnOpenShift {
    /**
     * Print pods, events and status when there are test failures. Fallback property `ts.openshift.print.info.on.error`.
     */
    boolean printInfoOnError() default true;

    /**
     * Delete project after running all the tests. Fallback property `ts.openshift.delete.project.after.all`.
     */
    boolean deleteProjectAfterAll() default true;

    /**
     * Run the tests using an ephemeral namespace that will be deleted afterwards. Fallback property
     * `ts.openshift.ephemeral.project.enabled`.
     */
    boolean ephemeralProjectEnabled() default true;

    /**
     * Load the additional resources before running all the tests. Fallback property
     * `ts.openshift.additional-resources`.
     */
    String[] additionalResources() default {};
}
