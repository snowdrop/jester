package io.jcloud.examples.spring.greetings;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.jcloud.api.JCloud;
import io.jcloud.api.Spring;

@JCloud
@Spring
public class GreetingApplicationIT {

    @Test
    public void testSpringApp() {
        given().get().then().body(is("Hello World"));
    }
}
