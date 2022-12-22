package io.jester.examples.quarkus.greetings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.jester.api.Jester;
import io.jester.api.Quarkus;
import io.jester.api.QuarkusServiceConfiguration;
import io.jester.api.RestService;
import io.jester.api.ServiceConfiguration;
import io.jester.examples.quarkus.greetings.samples.QuarkusPingApplication;

@Jester
@QuarkusServiceConfiguration(forService = "app", expectedLog = "this is wrong!")
@ServiceConfiguration(forService = "app", startupTimeout = "10s")
public class WrongConfigurationInAnnotationQuarkusIT {

    @Quarkus(classes = QuarkusPingApplication.class)
    static final RestService app = new RestService().withProperties("quarkus-ping-application.properties")
            .setAutoStart(false);

    @Test
    public void shouldFailBecauseExpectedLogIsWrong() {
        Assertions.assertThrows(RuntimeException.class, app::start,
                "Should fail because expected log in annotation is wrong");
    }
}
