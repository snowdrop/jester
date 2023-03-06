package io.github.snowdrop.jester.examples.quarkus.jdbc.mysql;

import javax.ws.rs.NotFoundException;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

// workaround for Quarkus providing its own NotFoundExceptionMapper
// which is more specific than our ApplicationExceptionMapper
public class NotFoundExceptionMapper {

    @ServerExceptionMapper
    public RestResponse<String> mapException(NotFoundException exception) {
        return new ApplicationExceptionMapper().mapException(exception);
    }
}
