package io.jester.test;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.jester.api.Dependency;
import io.jester.api.Jester;
import io.jester.api.Quarkus;
import io.jester.api.RestService;
import io.jester.test.samples.QuarkusPingApplication;

@Tag("native")
@Jester
public class NativeModeBootstrapQuarkusIT {
    @Quarkus(dependencies = @Dependency(artifactId = "quarkus-resteasy"), classes = QuarkusPingApplication.class)
    static final RestService app = new RestService().withProperties("quarkus-ping-application.properties")
            .withProperty("quarkus.package.type", "native");

    @Test
    public void shouldExecuteAppInNativeMode() {
        app.given().get("/ping").then().body(is("pong"));
    }
}
