package io.jcloud.test;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.jcloud.api.Dependency;
import io.jcloud.api.Quarkus;
import io.jcloud.api.RestService;
import io.jcloud.api.Scenario;
import io.jcloud.test.samples.QuarkusPingApplication;

@Scenario
public class ProdModeBootstrapQuarkusIT {
    @Quarkus(dependencies = @Dependency(artifactId = "quarkus-resteasy"), classes = QuarkusPingApplication.class)
    static final RestService app = new RestService().withProperties("quarkus-ping-application.properties");

    @Test
    public void shouldExecuteAppInProdMode() {
        app.given().get("/ping").then().body(is("pong"));
    }
}
