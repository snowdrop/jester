package io.github.snowdrop.jester.examples.spring.greetings;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.Spring;

@Jester
@Spring(forceBuild = true)
public class GreetingApplicationIT {

    @Test
    public void testSpringApp() {
        given().get("/greeting").then().body(is("Hello!"));
    }
}
