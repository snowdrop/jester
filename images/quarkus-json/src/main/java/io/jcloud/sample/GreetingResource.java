package io.jcloud.sample;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    public Hello get() {
        return new Hello("Hello Samples");
    }

    @POST
    public Hello update(Hello request) {
        return request;
    }

    @DELETE
    public void delete(Hello request) {
        // empty
    }
}