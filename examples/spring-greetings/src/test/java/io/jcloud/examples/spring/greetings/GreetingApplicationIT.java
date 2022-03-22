package io.jcloud.examples.spring.greetings;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.jcloud.api.Scenario;
import io.jcloud.api.Spring;

@Scenario
@Spring
public class GreetingApplicationIT {

    @Test
    public void testSpringApp() {
        given().get().then().body(is("Hello World"));
    }
}
