package io.jester.test;

import static io.jester.test.samples.ContainerSamples.QUARKUS_REST_IMAGE;
import static io.jester.test.samples.ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG;
import static io.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_PORT;
import static io.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH;
import static io.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH_OUTPUT;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import io.jester.api.Container;
import io.jester.api.Jester;
import io.jester.api.RestService;
import io.jester.utils.AwaitilityUtils;
import io.restassured.config.ConnectionConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;

@Tag("containers")
@Jester
public class NestedTestsIT {

    private static final String MY_PROPERTY = "my.property";
    private static final String MY_PROPERTY_EXPECTED_VALUE = "this is a custom property";

    static final AtomicInteger PRE_START_COUNTER = new AtomicInteger(0);
    static final AtomicInteger POST_START_COUNTER = new AtomicInteger(0);

    @Container(image = QUARKUS_REST_IMAGE, ports = SAMPLES_DEFAULT_PORT, expectedLog = QUARKUS_STARTUP_EXPECTED_LOG)
    RestService greetings = new RestService().withProperty(MY_PROPERTY, MY_PROPERTY_EXPECTED_VALUE)
            .onPreStart(s -> PRE_START_COUNTER.incrementAndGet())
            .onPostStart(s -> POST_START_COUNTER.incrementAndGet());

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class WithMethodLifecycle {

        @Test
        @Order(0)
        public void resetCounters() {
            PRE_START_COUNTER.set(0);
            POST_START_COUNTER.set(0);
        }

        @Test
        @Order(1)
        public void testServiceIsUpAndRunning() {
            assertEquals(1, PRE_START_COUNTER.get());
            assertEquals(1, POST_START_COUNTER.get());
            thenServiceIsUpAndRunning(greetings.given());
        }

        @Test
        @Order(2)
        public void testServiceIsRestartedAndRunning() {
            assertEquals(2, PRE_START_COUNTER.get());
            assertEquals(2, POST_START_COUNTER.get());
            thenServiceIsUpAndRunning(greetings.given());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class WithClassLifecycle {

        @Test
        @Order(0)
        public void resetCounters() {
            PRE_START_COUNTER.set(0);
            POST_START_COUNTER.set(0);
        }

        @Test
        @Order(1)
        public void testServiceIsUpAndRunning() {
            assertEquals(0, PRE_START_COUNTER.get());
            assertEquals(0, POST_START_COUNTER.get());
            thenServiceIsUpAndRunning(greetings.given());
        }

        @Test
        @Order(2)
        public void testServiceIsRestartedAndRunning() {
            assertEquals(0, PRE_START_COUNTER.get());
            assertEquals(0, POST_START_COUNTER.get());
            thenServiceIsUpAndRunning(greetings.given());
        }
    }

    private void thenServiceIsUpAndRunning(RequestSpecification given) {
        AwaitilityUtils
                .untilAsserted(
                        () -> given
                                .config(RestAssuredConfig.config()
                                        .httpClient(HttpClientConfig.httpClientConfig().reuseHttpClientInstance())
                                        .connectionConfig(ConnectionConfig.connectionConfig()
                                                .closeIdleConnectionsAfterEachResponse()))
                                .get(SAMPLES_DEFAULT_REST_PATH).then().statusCode(HttpStatus.SC_OK)
                                .body(is(SAMPLES_DEFAULT_REST_PATH_OUTPUT)));
    }
}
