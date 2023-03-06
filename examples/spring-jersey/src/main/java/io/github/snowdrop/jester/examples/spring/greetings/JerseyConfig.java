package io.github.snowdrop.jester.examples.spring.greetings;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(GreetingResource.class);
    }
}
