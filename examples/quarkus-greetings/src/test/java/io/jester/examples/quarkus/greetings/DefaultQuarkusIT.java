package io.jester.examples.quarkus.greetings;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.jester.api.Jester;
import io.jester.api.Quarkus;

@Jester
@Quarkus
public class DefaultQuarkusIT {

    @Test
    public void testDefaultRestServiceIsUpAndRunning() {
        given().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello, I'm victor"));
    }
}
