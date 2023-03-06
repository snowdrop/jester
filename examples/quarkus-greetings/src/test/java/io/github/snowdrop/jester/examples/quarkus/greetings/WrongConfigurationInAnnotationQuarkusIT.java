package io.github.snowdrop.jester.examples.quarkus.greetings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.QuarkusServiceConfiguration;
import io.github.snowdrop.jester.api.RestService;
import io.github.snowdrop.jester.api.ServiceConfiguration;
import io.github.snowdrop.jester.examples.quarkus.greetings.samples.QuarkusPingApplication;

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
