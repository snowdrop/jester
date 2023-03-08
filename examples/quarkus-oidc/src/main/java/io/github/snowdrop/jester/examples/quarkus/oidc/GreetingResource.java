package io.github.snowdrop.jester.examples.quarkus.oidc;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
@Authenticated
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello Samples";
    }
}
