package io.jcloud.examples.quarkus.greetings;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.jcloud.api.DisabledOnQuarkusNative;
import io.jcloud.api.JCloud;
import io.jcloud.api.Quarkus;
import io.jcloud.api.RestService;

@JCloud
public class UsingRuntimePropertiesIT {

    static final String JOSE_NAME = "jose";
    static final String MANUEL_NAME = "manuel";

    @Quarkus
    static RestService joseApp = new RestService().withProperty(ValidateCustomProperty.CUSTOM_PROPERTY, JOSE_NAME);
    @Quarkus
    static RestService manuelApp = new RestService().withProperty(ValidateCustomProperty.CUSTOM_PROPERTY, MANUEL_NAME);

    @Test
    public void shouldSayJose() {
        joseApp.given().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello, I'm " + JOSE_NAME));
    }

    @Test
    public void shouldSayManuel() {
        manuelApp.given().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello, I'm " + MANUEL_NAME));
    }

    @DisabledOnQuarkusNative
    @Test
    public void shouldLoadResources() {
        joseApp.given().get("/greeting/file").then().statusCode(HttpStatus.SC_OK).body(is("found!"));
        manuelApp.given().get("/greeting/file").then().statusCode(HttpStatus.SC_OK).body(is("found!"));
    }

}
