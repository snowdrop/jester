package io.github.snowdrop.jester.configuration;

public final class BenchmarkConfiguration {
    private String outputLocation = "target/benchmarks-results";

    public String getOutputLocation() {
        return outputLocation;
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }
}
