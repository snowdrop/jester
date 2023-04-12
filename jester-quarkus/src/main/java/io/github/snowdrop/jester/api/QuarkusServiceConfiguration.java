package io.github.snowdrop.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.snowdrop.jester.configuration.DeploymentMethod;

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

    /**
     * Relevant only for Kubernetes/OpenShift deployments. By default, if the Kubernetes/OpenShift extensions are
     * loaded, it will deploy the service using the generated resources by these extensions. Otherwise, it will use the
     * auto generated resources by Jester. Fallback service property: "ts.services.<SERVICE
     * NAME>.quarkus.deployment-method".
     */
    DeploymentMethod deploymentMethod() default DeploymentMethod.AUTO;
}
