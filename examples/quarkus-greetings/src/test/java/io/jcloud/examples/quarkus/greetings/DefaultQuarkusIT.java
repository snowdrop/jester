package io.jcloud.examples.quarkus.greetings;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.jcloud.api.Quarkus;
import io.jcloud.api.Scenario;

@Scenario
@Quarkus
public class DefaultQuarkusIT {

    @Test
    public void testDefaultRestServiceIsUpAndRunning() {
        given().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello, I'm victor"));
    }
}
