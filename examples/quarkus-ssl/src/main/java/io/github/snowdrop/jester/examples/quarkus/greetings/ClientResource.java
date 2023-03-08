package io.github.snowdrop.jester.examples.quarkus.greetings;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/greeting-from-client")
public class ClientResource {

    @Inject
    @RestClient
    Client client;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHelloFromClient() {
        return client.helloFromClient();
    }
}
