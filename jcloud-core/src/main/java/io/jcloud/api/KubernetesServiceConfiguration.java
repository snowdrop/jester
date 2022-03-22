package io.jcloud.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(KubernetesServiceConfigurations.class)
public @interface KubernetesServiceConfiguration {
    String forService();

    /**
     * Template for the initial deployment resource. Fallback service property: "ts.services.<SERVICE
     * NAME>.kubernetes.template".
     */
    String template() default "";

    /**
     * Use internal routing instead of exposed network interfaces. This is useful to integration several services that
     * are running as part of the same namespace or network. Fallback service property: "ts.services.<SERVICE
     * NAME>.kubernetes.use-internal-service-as-url".
     */
    boolean useInternalService() default false;
}
