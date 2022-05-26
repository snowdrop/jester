package io.jester.configuration;

public final class KubernetesConfiguration {
    private boolean printInfoOnError = true;
    private boolean deleteNamespaceAfterAll = true;
    private boolean ephemeralNamespaceEnabled = true;
    private String[] additionalResources;

    public boolean isPrintInfoOnError() {
        return printInfoOnError;
    }

    public void setPrintInfoOnError(boolean printInfoOnError) {
        this.printInfoOnError = printInfoOnError;
    }

    public boolean isDeleteNamespaceAfterAll() {
        return deleteNamespaceAfterAll;
    }

    public void setDeleteNamespaceAfterAll(boolean deleteNamespaceAfterAll) {
        this.deleteNamespaceAfterAll = deleteNamespaceAfterAll;
    }

    public boolean isEphemeralNamespaceEnabled() {
        return ephemeralNamespaceEnabled;
    }

    public void setEphemeralNamespaceEnabled(boolean ephemeralNamespaceEnabled) {
        this.ephemeralNamespaceEnabled = ephemeralNamespaceEnabled;
    }

    public String[] getAdditionalResources() {
        return additionalResources;
    }

    public void setAdditionalResources(String[] additionalResources) {
        this.additionalResources = additionalResources;
    }
}
