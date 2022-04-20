package io.jcloud.examples.spring.greetings;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.jcloud.api.JCloud;
import io.jcloud.api.RestService;
import io.jcloud.api.ServiceConfiguration;
import io.jcloud.api.Spring;

@JCloud
@ServiceConfiguration(forService = "app", deleteFolderOnClose = false)
public class SpringWithSslIT {

    @Spring
    RestService app = new RestService();

    @Test
    public void testHttps() {
        app.https().get().then().statusCode(HttpStatus.SC_OK).body(is("Hello ssl!"));
    }
}
