package io.jester.test;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.jester.api.Dependency;
import io.jester.api.Jester;
import io.jester.api.Quarkus;
import io.jester.api.QuarkusServiceConfiguration;
import io.jester.api.RestService;
import io.jester.test.samples.AppLifecycleBean;
import io.jester.test.samples.QuarkusPingApplication;

@Jester
@QuarkusServiceConfiguration(forService = "one", expectedLog = "Installed features: (.*), resteasy, (.*)")
@QuarkusServiceConfiguration(forService = "two", expectedLog = "Custom log at startup event!")
public class MultipleAppsWithConfigInAnnotationsQuarkusIT {

    @Quarkus(dependencies = @Dependency(artifactId = "quarkus-resteasy"), classes = QuarkusPingApplication.class)
    static final RestService one = new RestService().withProperties("quarkus-ping-application.properties");

    @Quarkus(dependencies = @Dependency(artifactId = "quarkus-resteasy"), classes = { QuarkusPingApplication.class,
            AppLifecycleBean.class })
    static final RestService two = new RestService().withProperties("quarkus-ping-application.properties");

    @Test
    public void shouldExecuteAppInProdMode() {
        one.given().get("/ping").then().body(is("pong"));
        two.given().get("/ping").then().body(is("pong"));
    }
}
