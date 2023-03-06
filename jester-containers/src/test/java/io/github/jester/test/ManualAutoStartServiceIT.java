package io.github.jester.test;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.github.jester.api.Container;
import io.github.jester.api.Jester;
import io.github.jester.api.RestService;
import io.github.jester.test.samples.ContainerSamples;

@Tag("containers")
@Jester
public class ManualAutoStartServiceIT {

    private static final AtomicInteger PRE_START_COUNTER = new AtomicInteger(0);

    @Container(image = ContainerSamples.QUARKUS_REST_IMAGE, ports = ContainerSamples.SAMPLES_DEFAULT_PORT, expectedLog = ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG)
    static RestService greetings = new RestService().setAutoStart(false)
            .onPreStart((s) -> PRE_START_COUNTER.incrementAndGet());

    @Test
    public void shouldBeStopped() {
        assertEquals(0, PRE_START_COUNTER.get(), "service.onPreStart() was called!");
        Assertions.assertFalse(greetings.isRunning(), "Service was up and running!");
        greetings.start();
        Assertions.assertTrue(greetings.isRunning(), "Service was not up and running!");
        greetings.given().get(ContainerSamples.SAMPLES_DEFAULT_REST_PATH).then().statusCode(HttpStatus.SC_OK)
                .body(Matchers.is(ContainerSamples.SAMPLES_DEFAULT_REST_PATH_OUTPUT));
    }
}
