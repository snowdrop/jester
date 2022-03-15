package io.quarkus.qe;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.jcloud.api.Quarkus;
import io.jcloud.api.RestService;
import io.jcloud.api.Scenario;

@Scenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FailAtStartupIT {

    static final String MANUEL_NAME = "manuel";

    @Quarkus
    static final RestService app = new RestService()
            .withProperty(ValidateCustomProperty.CUSTOM_PROPERTY, ValidateCustomProperty.DISALLOW_PROPERTY_VALUE)
            .setAutoStart(false);

    @Order(1)
    @Test
    public void shouldBeStopped() {
        assertFalse(app.isRunning(), "Autostart is not working!");
    }

    @Order(2)
    @Test
    public void shouldFailOnStart() {
        assertThrows(RuntimeException.class, () -> app.start(),
                "Should fail because runtime exception in ValidateCustomProperty");
        app.logs().assertContains("java.lang.RuntimeException: Wrong value! WRONG!");
    }

    @Order(3)
    @Test
    public void shouldWorkWhenPropertyIsCorrect() {
        app.withProperty(ValidateCustomProperty.CUSTOM_PROPERTY, MANUEL_NAME);
        app.start();
        app.given().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello, I'm " + MANUEL_NAME));
    }
}
