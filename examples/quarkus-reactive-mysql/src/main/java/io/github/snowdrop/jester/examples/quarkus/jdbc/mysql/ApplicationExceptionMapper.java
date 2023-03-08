package io.github.snowdrop.jester.examples.quarkus.jdbc.mysql;

import java.util.logging.Logger;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jboss.resteasy.reactive.server.jaxrs.RestResponseBuilderImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

public class ApplicationExceptionMapper {
    @ServerExceptionMapper
    public RestResponse<String> mapException(Exception exception) {
        Logger.getAnonymousLogger().severe("PRINT ApplicationExceptionMapper ("
                + (exception instanceof WebApplicationException) + "): " + exception.getMessage());
        int code = RestResponse.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        if (exception instanceof WebApplicationException) {
            Logger.getAnonymousLogger()
                    .severe("code: " + ((WebApplicationException) exception).getResponse().getStatus());
            code = ((WebApplicationException) exception).getResponse().getStatus();
        }
        return RestResponseBuilderImpl
                .create(RestResponse.Status.INTERNAL_SERVER_ERROR, new ObjectMapper().createObjectNode()
                        .put("code", code).put("error", exception.getMessage()).toString())
                .type(MediaType.APPLICATION_JSON).status(code).build();
    }
}
