package io.github.snowdrop.jester.configuration;

public enum DeploymentMethod {
    /**
     * This strategy will try to use the Kubernetes/OpenShift extension if loaded. Otherwise, it will use the embedded
     * method.
     */
    AUTO,
    /**
     * This strategy will build the deployment resources.
     */
    EMBEDDED,
    /**
     * This strategy will always use the Kubernetes/OpenShift extension.
     */
    USING_EXTENSION
}
