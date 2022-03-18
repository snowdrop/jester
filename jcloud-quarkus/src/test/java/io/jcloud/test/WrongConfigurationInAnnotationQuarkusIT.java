package io.jcloud.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.jcloud.api.Dependency;
import io.jcloud.api.Quarkus;
import io.jcloud.api.QuarkusServiceConfiguration;
import io.jcloud.api.RestService;
import io.jcloud.api.Scenario;
import io.jcloud.api.ServiceConfiguration;
import io.jcloud.test.samples.QuarkusPingApplication;

@Scenario
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
