package io.github.snowdrop.jester.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.ServiceConfiguration;

@Jester
@Quarkus(location = "../images/quarkus-rest")
@ServiceConfiguration(forService = "quarkus", deleteFolderOnClose = false)
public class DifferentLocationQuarkusIT {

    @Test
    public void shouldExecuteAppInProdMode() {
        given().get("/hello").then().statusCode(HttpStatus.SC_OK).body(is("Hello Samples"));
    }
}
