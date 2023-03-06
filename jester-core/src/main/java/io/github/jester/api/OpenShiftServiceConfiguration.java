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
@Repeatable(OpenShiftServiceConfigurations.class)
public @interface OpenShiftServiceConfiguration {
    String forService();

    /**
     * Template for the initial deployment resource. Fallback service property: "ts.services.<SERVICE
     * NAME>.openshift.template".
     */
    String template() default "";

    /**
     * Use internal routing instead of exposed network interfaces. This is useful to integration several services that
     * are running as part of the same namespace or network. Fallback service property: "ts.services.<SERVICE
     * NAME>.openshift.use-internal-service".
     */
    boolean useInternalService() default false;

    /**
     * Map additional ports to be used during the tests.
     */
    int[] additionalPorts() default {};

    /**
     * Expose the application using an OpenShift route. Fallback property `ts.services.<SERVICE
     * NAME>.openshift.use-route`.
     */
    boolean useRoute() default false;
}
