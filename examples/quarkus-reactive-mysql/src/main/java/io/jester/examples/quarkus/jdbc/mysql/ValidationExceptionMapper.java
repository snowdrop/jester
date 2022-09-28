package io.jester.examples.quarkus.jdbc.mysql;

import java.util.logging.Logger;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jboss.resteasy.reactive.server.jaxrs.RestResponseBuilderImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ValidationExceptionMapper {

    public static final int UNPROCESSABLE_ENTITY = 422;

    @ServerExceptionMapper
    public RestResponse<String> mapException(ConstraintViolationException exception) {
        Logger.getAnonymousLogger().severe("PRINT ConstraintViolationException: " + exception);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode errors = mapper.createArrayNode();

        for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
            errors.addObject().put("path", constraintViolation.getPropertyPath().toString()).put("message",
                    constraintViolation.getMessage());
        }
        Logger.getAnonymousLogger().severe("errors: " + errors.asText());
        return RestResponseBuilderImpl
                .create(RestResponse.Status.INTERNAL_SERVER_ERROR,
                        mapper.createObjectNode().put("code", UNPROCESSABLE_ENTITY).set("error", errors).toString())
                .status(UNPROCESSABLE_ENTITY).build();
    }
}
