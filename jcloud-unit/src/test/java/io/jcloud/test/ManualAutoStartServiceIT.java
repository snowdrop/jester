package io.jcloud.test;

import static io.jcloud.test.samples.ContainerSamples.QUARKUS_REST_EXPECTED_LOG;
import static io.jcloud.test.samples.ContainerSamples.QUARKUS_REST_IMAGE;
import static io.jcloud.test.samples.ContainerSamples.QUARKUS_REST_PATH;
import static io.jcloud.test.samples.ContainerSamples.QUARKUS_REST_PATH_OUTPUT;
import static io.jcloud.test.samples.ContainerSamples.QUARKUS_REST_PORT;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.jcloud.api.Container;
import io.jcloud.api.RestService;
import io.jcloud.api.Scenario;

@Scenario
public class ManualAutoStartServiceIT {

    private static final AtomicInteger PRE_START_COUNTER = new AtomicInteger(0);

    @Container(image = QUARKUS_REST_IMAGE, ports = QUARKUS_REST_PORT, expectedLog = QUARKUS_REST_EXPECTED_LOG)
    static RestService greetings = new RestService()
            .setAutoStart(false)
            .onPreStart((s) -> PRE_START_COUNTER.incrementAndGet());

    @Test
    public void shouldBeStopped() {
        assertEquals(0, PRE_START_COUNTER.get(), "service.onPreStart() was called!");
        assertFalse(greetings.isRunning(), "Service was up and running!");
        greetings.start();
        assertTrue(greetings.isRunning(), "Service was not up and running!");
        greetings.given().get(QUARKUS_REST_PATH).then().statusCode(HttpStatus.SC_OK).body(is(QUARKUS_REST_PATH_OUTPUT));
    }
}
