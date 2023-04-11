package io.github.snowdrop.jester.configuration;

public final class OpenShiftServiceConfiguration {
    private String template;
    private boolean useInternalService = false;
    private int[] additionalPorts;
    private boolean useRoute = false;
    private String serviceAccount;

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

    public boolean isUseRoute() {
        return useRoute;
    }

    public void setUseRoute(boolean useRoute) {
        this.useRoute = useRoute;
    }

    public String getServiceAccount() {
        return serviceAccount;
    }

    public void setServiceAccount(String serviceAccount) {
        this.serviceAccount = serviceAccount;
    }
}
