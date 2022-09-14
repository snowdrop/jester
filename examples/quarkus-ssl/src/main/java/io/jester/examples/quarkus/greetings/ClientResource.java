package io.jester.examples.quarkus.greetings;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

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
