package io.github.snowdrop.jester.examples.quarkus.grpc;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.RestService;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

@Jester
public class MultiplePortsQuarkusIT {

    static final String NAME = "Victor";

    @Quarkus
    static final RestService app = new RestService();

    @Test
    public void shouldPortForGrpcBeExposed() {
        HelloRequest request = HelloRequest.newBuilder().setName("Victor").build();
        HelloReply response = GreeterGrpc.newBlockingStub(grpcChannel()).sayHello(request);

        assertEquals("Hello " + NAME, response.getMessage());
    }

    @Test
    public void shouldPortForHttpBeExposed() {
        app.given().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello!"));
    }

    private Channel grpcChannel() {
        return ManagedChannelBuilder.forAddress(app.getHost(), app.getMappedPort(9001)).usePlaintext().build();
    }

}
