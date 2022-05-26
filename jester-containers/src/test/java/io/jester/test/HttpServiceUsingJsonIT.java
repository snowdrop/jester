package io.jester.test;

import static io.jester.test.samples.ContainerSamples.QUARKUS_JSON_IMAGE;
import static io.jester.test.samples.ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG;
import static io.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_JSON_PATH;
import static io.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_JSON_PATH_OUTPUT;
import static io.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.jester.api.Container;
import io.jester.api.HttpService;
import io.jester.api.Jester;

@Tag("containers")
@Jester
public class HttpServiceUsingJsonIT {

    @Container(image = QUARKUS_JSON_IMAGE, ports = SAMPLES_DEFAULT_PORT, expectedLog = QUARKUS_STARTUP_EXPECTED_LOG)
    static HttpService json = new HttpService();

    @Test
    public void testGet() {
        HttpResponse<Supplier<Hello>> response = json.getAsJson(Hello.class, SAMPLES_DEFAULT_JSON_PATH);
        assertEquals(SAMPLES_DEFAULT_JSON_PATH_OUTPUT, response.body().get().message);
    }

    @Test
    public void testGetUsingMap() {
        HttpResponse<Supplier<Map>> response = json.getAsJson(SAMPLES_DEFAULT_JSON_PATH);
        assertEquals(SAMPLES_DEFAULT_JSON_PATH_OUTPUT, response.body().get().get("message"));
    }

    @Test
    public void testPost() {
        Hello request = new Hello("expected");
        HttpResponse<Supplier<Hello>> response = json.postAsJson(request, Hello.class, SAMPLES_DEFAULT_JSON_PATH);
        assertEquals(request.message, response.body().get().message);
    }

    @Test
    public void testPostUsingMap() {
        Hello request = new Hello("expected");
        HttpResponse<Supplier<Map>> response = json.postAsJson(request, SAMPLES_DEFAULT_JSON_PATH);
        assertEquals(request.message, response.body().get().get("message"));
    }

    @Test
    public void testDelete() {
        HttpResponse<InputStream> response = json.delete(SAMPLES_DEFAULT_JSON_PATH);
        assertNotNull(response.body());
    }

    public static class Hello {
        private String message;

        public Hello() {

        }

        public Hello(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
