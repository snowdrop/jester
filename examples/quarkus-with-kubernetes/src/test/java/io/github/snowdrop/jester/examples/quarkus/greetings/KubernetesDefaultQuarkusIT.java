package io.github.snowdrop.jester.examples.quarkus.greetings;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.RunOnKubernetes;

@Jester
@Quarkus
@RunOnKubernetes
public class KubernetesDefaultQuarkusIT {
    @Test
    public void testDefaultRestServiceIsUpAndRunning() {
        given().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello, I'm victor"));
    }
}
