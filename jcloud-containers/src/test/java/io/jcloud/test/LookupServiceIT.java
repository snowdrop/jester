package io.jcloud.test;

import static io.jcloud.test.samples.ContainerSamples.QUARKUS_REST_IMAGE;
import static io.jcloud.test.samples.ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_PORT;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH_OUTPUT;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.jcloud.api.Container;
import io.jcloud.api.LookupService;
import io.jcloud.api.RestService;
import io.jcloud.api.Scenario;

@Scenario
public class LookupServiceIT extends BaseLookupServiceIT {

    @Container(image = QUARKUS_REST_IMAGE, ports = SAMPLES_DEFAULT_PORT, expectedLog = QUARKUS_STARTUP_EXPECTED_LOG)
    static RestService createdInOuter = new RestService();
}

abstract class BaseLookupServiceIT {

    @LookupService
    static RestService createdInOuter;

    @Container(image = QUARKUS_REST_IMAGE, ports = SAMPLES_DEFAULT_PORT, expectedLog = QUARKUS_STARTUP_EXPECTED_LOG)
    static RestService createdInInner = new RestService();

    @Test
    public void testInnerOuterServicesAreRunning() {
        createdInOuter.given().get(SAMPLES_DEFAULT_REST_PATH).then().statusCode(HttpStatus.SC_OK)
                .body(is(SAMPLES_DEFAULT_REST_PATH_OUTPUT));
        createdInInner.given().get(SAMPLES_DEFAULT_REST_PATH).then().statusCode(HttpStatus.SC_OK)
                .body(is(SAMPLES_DEFAULT_REST_PATH_OUTPUT));
    }

}