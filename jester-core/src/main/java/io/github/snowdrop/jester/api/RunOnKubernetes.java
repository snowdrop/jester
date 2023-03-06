package io.github.snowdrop.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RunOnKubernetes {
    /**
     * Print pods, events and status when there are test failures. Fallback property
     * `ts.kubernetes.print.info.on.error`.
     */
    boolean printInfoOnError() default true;

    /**
     * Delete namespace after running all the tests. Fallback property `ts.kubernetes.delete.namespace.after.all`.
     */
    boolean deleteNamespaceAfterAll() default true;

    /**
     * Run the tests on Kubernetes in an ephemeral namespace that will be deleted afterwards. Fallback property
     * `ts.kubernetes.ephemeral.namespaces.enabled`.
     */
    boolean ephemeralNamespaceEnabled() default true;

    /**
     * Load the additional resources before running all the tests. Fallback property
     * `ts.kubernetes.additional-resources`.
     */
    String[] additionalResources() default {};
}
