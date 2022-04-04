package io.jcloud.test;

import static io.jcloud.test.samples.ContainerSamples.QUARKUS_REST_IMAGE;
import static io.jcloud.test.samples.ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_PORT;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.jcloud.api.Container;
import io.jcloud.api.HttpService;
import io.jcloud.api.JCloud;

@Tag("containers")
@JCloud
public class HttpServiceIT {

    @Container(image = QUARKUS_REST_IMAGE, ports = SAMPLES_DEFAULT_PORT, expectedLog = QUARKUS_STARTUP_EXPECTED_LOG)
    static HttpService greetings = new HttpService();

    @Test
    public void testGet() {
        assertGetResource();
    }

    @Test
    public void testGetUsingCustomClient() {
        greetings.withHttpClient(HttpClient.newHttpClient());
        assertGetResource();
    }

    @Test
    public void testGetUsingConcurrentCalls() {
        greetings.withConcurrentCalls(10);
        assertGetResource();
    }

    private void assertGetResource() {
        HttpResponse<String> response = greetings.getString(SAMPLES_DEFAULT_REST_PATH);
        assertEquals(SAMPLES_DEFAULT_REST_PATH_OUTPUT, response.body());
    }
}
