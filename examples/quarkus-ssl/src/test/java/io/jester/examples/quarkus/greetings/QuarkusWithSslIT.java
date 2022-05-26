package io.jester.examples.quarkus.greetings;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.jester.api.Jester;
import io.jester.api.Quarkus;
import io.jester.api.RestService;

@Jester
public class QuarkusWithSslIT {

    @Quarkus
    RestService app = new RestService();

    @Test
    public void testHttps() {
        app.https().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello ssl!"));
    }
}
