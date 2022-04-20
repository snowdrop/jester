package io.jcloud.examples.quarkus.greetings;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.jcloud.api.JCloud;
import io.jcloud.api.Quarkus;
import io.jcloud.api.RestService;

@JCloud
public class QuarkusWithSslIT {

    @Quarkus
    RestService app = new RestService();

    @Test
    public void testHttps() {
        app.https().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello ssl!"));
    }
}
