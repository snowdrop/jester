package io.github.snowdrop.jester.configuration;

public final class QuarkusServiceConfiguration {
    private String expectedLog = "Installed features";
    private DeploymentMethod deploymentMethod = DeploymentMethod.AUTO;

    public String getExpectedLog() {
        return expectedLog;
    }

    public void setExpectedLog(String expectedLog) {
        this.expectedLog = expectedLog;
    }

    public DeploymentMethod getDeploymentMethod() {
        return deploymentMethod;
    }

    public void setDeploymentMethod(DeploymentMethod deploymentMethod) {
        this.deploymentMethod = deploymentMethod;
    }
}
