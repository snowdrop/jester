package io.github.snowdrop.jester.examples.quarkus.greetings;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "client")
public interface Client {
    @GET
    @Path("/greeting")
    @Produces(MediaType.TEXT_PLAIN)
    String helloFromClient();
}
