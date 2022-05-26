package io.jester.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.jester.api.Dependency;
import io.jester.api.Jester;
import io.jester.api.Quarkus;
import io.jester.api.QuarkusServiceConfiguration;
import io.jester.api.RestService;
import io.jester.api.ServiceConfiguration;
import io.jester.test.samples.QuarkusPingApplication;

@Jester
@QuarkusServiceConfiguration(forService = "app", expectedLog = "this is wrong!")
@ServiceConfiguration(forService = "app", startupTimeout = "10s")
public class WrongConfigurationInAnnotationQuarkusIT {

    @Quarkus(dependencies = @Dependency(artifactId = "quarkus-resteasy"), classes = QuarkusPingApplication.class)
    static final RestService app = new RestService().withProperties("quarkus-ping-application.properties")
            .setAutoStart(false);

    @Test
    public void shouldFailBecauseExpectedLogIsWrong() {
        assertThrows(RuntimeException.class, app::start, "Should fail because expected log in annotation is wrong");
    }
}
