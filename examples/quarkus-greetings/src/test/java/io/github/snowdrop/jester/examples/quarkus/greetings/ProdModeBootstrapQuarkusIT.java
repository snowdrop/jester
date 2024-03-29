package io.github.snowdrop.jester.examples.quarkus.greetings;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.RestService;
import io.github.snowdrop.jester.examples.quarkus.greetings.samples.QuarkusPingApplication;

@Jester
public class ProdModeBootstrapQuarkusIT {
    @Quarkus(classes = QuarkusPingApplication.class)
    static final RestService app = new RestService().withProperties("quarkus-ping-application.properties");

    @Test
    public void shouldExecuteAppInProdMode() {
        app.given().get("/ping").then().body(Matchers.is("pong"));
    }
}
