package io.jester.configuration;

public final class OpenShiftConfiguration {
    private boolean printInfoOnError = true;
    private boolean deleteProjectAfterAll = true;
    private boolean ephemeralStorageEnabled = true;
    private String[] additionalResources;

    public boolean isPrintInfoOnError() {
        return printInfoOnError;
    }

    public void setPrintInfoOnError(boolean printInfoOnError) {
        this.printInfoOnError = printInfoOnError;
    }

    public boolean isDeleteProjectAfterAll() {
        return deleteProjectAfterAll;
    }

    public void setDeleteProjectAfterAll(boolean deleteProjectAfterAll) {
        this.deleteProjectAfterAll = deleteProjectAfterAll;
    }

    public boolean isEphemeralStorageEnabled() {
        return ephemeralStorageEnabled;
    }

    public void setEphemeralStorageEnabled(boolean ephemeralStorageEnabled) {
        this.ephemeralStorageEnabled = ephemeralStorageEnabled;
    }

    public String[] getAdditionalResources() {
        return additionalResources;
    }

    public void setAdditionalResources(String[] additionalResources) {
        this.additionalResources = additionalResources;
    }
}
