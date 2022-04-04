package io.jcloud.test;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.jcloud.api.Dependency;
import io.jcloud.api.JCloud;
import io.jcloud.api.Quarkus;
import io.jcloud.api.QuarkusServiceConfiguration;
import io.jcloud.api.RestService;
import io.jcloud.test.samples.AppLifecycleBean;
import io.jcloud.test.samples.QuarkusPingApplication;

@JCloud
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
