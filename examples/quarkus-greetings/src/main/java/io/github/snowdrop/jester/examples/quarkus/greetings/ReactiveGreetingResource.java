package io.github.snowdrop.jester.examples.quarkus.greetings;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/reactive-greeting")
public class ReactiveGreetingResource {

    @ConfigProperty(name = ValidateCustomProperty.CUSTOM_PROPERTY)
    String name;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> sayHello() {
        return Uni.createFrom().item("Hello, I'm " + name);
    }
}
