package io.github.snowdrop.jester.test;

import static io.github.snowdrop.jester.test.samples.ContainerSamples.QUARKUS_REST_IMAGE;
import static io.github.snowdrop.jester.test.samples.ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG;
import static io.github.snowdrop.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_PORT;
import static io.github.snowdrop.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH;
import static io.github.snowdrop.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH_OUTPUT;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.github.snowdrop.jester.api.Container;
import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.LookupService;
import io.github.snowdrop.jester.api.RestService;

@Tag("containers")
@Jester
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
