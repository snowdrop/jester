package io.github.jester.examples.quarkus;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.github.jester.api.Dependency;
import io.github.jester.api.Jester;
import io.github.jester.api.Quarkus;
import io.github.jester.api.RestService;
import io.github.jester.examples.quarkus.samples.QuarkusPingApplication;

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
