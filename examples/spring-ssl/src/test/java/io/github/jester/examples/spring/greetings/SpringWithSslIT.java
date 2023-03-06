package io.github.jester.examples.spring.greetings;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.github.jester.api.Jester;
import io.github.jester.api.RestService;
import io.github.jester.api.ServiceConfiguration;
import io.github.jester.api.Spring;

@Jester
@ServiceConfiguration(forService = "app", deleteFolderOnClose = false)
public class SpringWithSslIT {

    @Spring
    RestService app = new RestService();

    @Test
    public void testHttps() {
        app.https().get().then().statusCode(HttpStatus.SC_OK).body(is("Hello ssl!"));
    }
}
