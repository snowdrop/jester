package io.jcloud.test.samples;

public final class ContainerSamples {
    /**
     * Sources of sample in <a href="https://github.com/snowdrop/jcloud-unit/samples/quarkus-rest">here</a>
     * If you don't have installed the samples in your local machine, execute: `mvn clean install` at ../samples.
     */
    public static final String QUARKUS_REST_IMAGE = "${sample.quarkus-rest.image:server.io/test/quarkus-rest:latest}";
    public static final int QUARKUS_REST_PORT = 8080;
    public static final String QUARKUS_REST_EXPECTED_LOG = "Installed features";
    public static final String QUARKUS_REST_PATH = "/hello";
    public static final String QUARKUS_REST_PATH_OUTPUT = "Hello Quarkus";

    private ContainerSamples() {

    }
}
