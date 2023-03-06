package io.github.snowdrop.jester.examples.quarkus.greetings;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.RestService;
import io.github.snowdrop.jester.utils.Ports;

@Jester
public class QuarkusWithSslIT {

    @Quarkus(classes = { GreetingResource.class, CustomNoopHostnameVerifier.class })
    RestService app = new RestService().withProperties("server.properties");

    @Quarkus(classes = { Client.class, ClientResource.class, CustomNoopHostnameVerifier.class })
    RestService client = new RestService().withProperties("client.properties").withProperty(
            "quarkus.rest-client.client.url", () -> "https://localhost:" + app.getMappedPort(Ports.DEFAULT_SSL_PORT));

    @Test
    public void testHttps() {
        app.https().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello ssl!"));
    }

    @Test
    public void testHttpsInRestClient() {
        client.given().get("/greeting-from-client").then().statusCode(HttpStatus.SC_OK).body(is("Hello ssl!"));
    }
}
