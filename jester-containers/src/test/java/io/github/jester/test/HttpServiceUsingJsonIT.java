package io.github.jester.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.github.jester.api.Container;
import io.github.jester.api.HttpService;
import io.github.jester.api.Jester;
import io.github.jester.test.samples.ContainerSamples;

@Tag("containers")
@Jester
public class HttpServiceUsingJsonIT {

    @Container(image = ContainerSamples.QUARKUS_JSON_IMAGE, ports = ContainerSamples.SAMPLES_DEFAULT_PORT, expectedLog = ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG)
    static HttpService json = new HttpService();

    @Test
    public void testGet() {
        HttpResponse<Supplier<Hello>> response = json.getAsJson(Hello.class,
                ContainerSamples.SAMPLES_DEFAULT_JSON_PATH);
        Assertions.assertEquals(ContainerSamples.SAMPLES_DEFAULT_JSON_PATH_OUTPUT, response.body().get().message);
    }

    @Test
    public void testGetUsingMap() {
        HttpResponse<Supplier<Map>> response = json.getAsJson(ContainerSamples.SAMPLES_DEFAULT_JSON_PATH);
        Assertions.assertEquals(ContainerSamples.SAMPLES_DEFAULT_JSON_PATH_OUTPUT,
                response.body().get().get("message"));
    }

    @Test
    public void testPost() {
        Hello request = new Hello("expected");
        HttpResponse<Supplier<Hello>> response = json.postAsJson(request, Hello.class,
                ContainerSamples.SAMPLES_DEFAULT_JSON_PATH);
        assertEquals(request.message, response.body().get().message);
    }

    @Test
    public void testPostUsingMap() {
        Hello request = new Hello("expected");
        HttpResponse<Supplier<Map>> response = json.postAsJson(request, ContainerSamples.SAMPLES_DEFAULT_JSON_PATH);
        assertEquals(request.message, response.body().get().get("message"));
    }

    @Test
    public void testDelete() {
        HttpResponse<InputStream> response = json.delete(ContainerSamples.SAMPLES_DEFAULT_JSON_PATH);
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
