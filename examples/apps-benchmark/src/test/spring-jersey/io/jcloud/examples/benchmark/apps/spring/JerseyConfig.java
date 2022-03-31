package io.jcloud.examples.benchmark.apps.spring;

import javax.annotation.PostConstruct;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JerseyConfig extends ResourceConfig {

    @PostConstruct
    public void init() {
        register(io.jcloud.examples.benchmark.apps.GreetingResource.class);
    }
}
