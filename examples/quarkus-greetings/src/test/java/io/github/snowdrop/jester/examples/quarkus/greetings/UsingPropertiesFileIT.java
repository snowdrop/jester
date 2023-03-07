package io.github.snowdrop.jester.examples.quarkus.greetings;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.RestService;

public class UsingPropertiesFileIT {

    static final String JOSE_NAME = "jose";
    static final String MANUEL_NAME = "manuel";

    @Quarkus
    static final RestService joseApp = new RestService().withProperties("jose.properties");
    @Quarkus
    static final RestService manuelApp = new RestService().withProperties("manuel.properties");

    @Test
    public void shouldSayJose() {
        joseApp.given().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello, I'm " + JOSE_NAME));
    }

    @Test
    public void shouldSayManuel() {
        manuelApp.given().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello, I'm " + MANUEL_NAME));
    }

}
