package io.jcloud.test.samples;

/**
 * Sources of samples in <a href="https://github.com/snowdrop/jcloud-unit/samples">here</a> If you don't have installed
 * the samples in your local machine, execute: `mvn clean install` at ../samples.
 */
public final class ContainerSamples {
    public static final String QUARKUS_REST_IMAGE = "${sample.quarkus-rest.image:server.io/test/quarkus-rest:latest}";
    public static final String QUARKUS_REST_LOCATION = "../images/quarkus-rest";

    public static final int SAMPLES_DEFAULT_PORT = 8080;
    public static final String QUARKUS_STARTUP_EXPECTED_LOG = "Installed features";
    public static final String SAMPLES_DEFAULT_REST_PATH = "/hello";
    public static final String SAMPLES_DEFAULT_REST_PATH_OUTPUT = "Hello Samples";

    private ContainerSamples() {

    }
}
