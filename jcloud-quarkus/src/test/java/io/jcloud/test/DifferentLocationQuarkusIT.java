package io.jcloud.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.jcloud.api.JCloud;
import io.jcloud.api.Quarkus;
import io.jcloud.api.ServiceConfiguration;

@JCloud
@Quarkus(location = "../images/quarkus-rest")
@ServiceConfiguration(forService = "quarkus", deleteFolderOnClose = false)
public class DifferentLocationQuarkusIT {

    @Test
    public void shouldExecuteAppInProdMode() {
        given().get("/hello").then().statusCode(HttpStatus.SC_OK).body(is("Hello Samples"));
    }
}
