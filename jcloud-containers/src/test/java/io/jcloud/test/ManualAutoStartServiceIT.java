package io.jcloud.test;

import static io.jcloud.test.samples.ContainerSamples.QUARKUS_REST_IMAGE;
import static io.jcloud.test.samples.ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_PORT;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH_OUTPUT;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.jcloud.api.Container;
import io.jcloud.api.JCloud;
import io.jcloud.api.RestService;

@Tag("containers")
@JCloud
public class ManualAutoStartServiceIT {

    private static final AtomicInteger PRE_START_COUNTER = new AtomicInteger(0);

    @Container(image = QUARKUS_REST_IMAGE, ports = SAMPLES_DEFAULT_PORT, expectedLog = QUARKUS_STARTUP_EXPECTED_LOG)
    static RestService greetings = new RestService().setAutoStart(false)
            .onPreStart((s) -> PRE_START_COUNTER.incrementAndGet());

    @Test
    public void shouldBeStopped() {
        assertEquals(0, PRE_START_COUNTER.get(), "service.onPreStart() was called!");
        assertFalse(greetings.isRunning(), "Service was up and running!");
        greetings.start();
        assertTrue(greetings.isRunning(), "Service was not up and running!");
        greetings.given().get(SAMPLES_DEFAULT_REST_PATH).then().statusCode(HttpStatus.SC_OK)
                .body(is(SAMPLES_DEFAULT_REST_PATH_OUTPUT));
    }
}
