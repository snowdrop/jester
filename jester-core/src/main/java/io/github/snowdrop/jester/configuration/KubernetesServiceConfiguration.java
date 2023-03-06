package io.github.snowdrop.jester.configuration;

public final class KubernetesServiceConfiguration {
    private String template;
    private boolean useInternalService = false;
    private int[] additionalPorts;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isUseInternalService() {
        return useInternalService;
    }

    public void setUseInternalService(boolean useInternalService) {
        this.useInternalService = useInternalService;
    }

    public int[] getAdditionalPorts() {
        return additionalPorts;
    }

    public void setAdditionalPorts(int[] additionalPorts) {
        this.additionalPorts = additionalPorts;
    }
}
