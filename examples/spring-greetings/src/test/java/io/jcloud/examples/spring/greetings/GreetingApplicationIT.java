package io.jcloud.examples.spring.greetings;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.jcloud.api.RestService;
import io.jcloud.api.Scenario;
import io.jcloud.api.Spring;

@Scenario
public class GreetingApplicationIT {

    @Spring
    static final RestService app = new RestService();

    @Test
    public void testSpringApp() {
        app.given().get().then().body(is("Hello World"));
    }
}
