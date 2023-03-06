package io.github.snowdrop.jester.examples.quarkus.greetings;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "client")
public interface Client {
    @GET
    @Path("/greeting")
    @Produces(MediaType.TEXT_PLAIN)
    String helloFromClient();
}
