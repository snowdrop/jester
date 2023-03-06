package io.github.snowdrop.jester.api;

import static io.github.snowdrop.jester.utils.Ports.DEFAULT_HTTP_PORT;
import static io.github.snowdrop.jester.utils.Ports.DEFAULT_SSL_PORT;

import io.github.snowdrop.jester.core.BaseService;
import io.github.snowdrop.jester.logging.Log;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class RestService extends BaseService<RestService> {

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final String BASE_PATH = "/";

    private final int httpPort;
    private final int sslPort;
    private final String basePath;

    public RestService() {
        this(DEFAULT_HTTP_PORT, DEFAULT_SSL_PORT, BASE_PATH);
    }

    public RestService(int httpPort, int sslPort, String basePath) {
        this.httpPort = httpPort;
        this.sslPort = sslPort;
        this.basePath = basePath;
    }

    public RequestSpecification given() {
        return RestAssured.given().baseUri(HTTP + getHost()).basePath(basePath).port(getMappedPort(httpPort));
    }

    public RequestSpecification https() {
        return RestAssured.given().baseUri(HTTPS + getHost()).basePath(basePath).port(getMappedPort(sslPort)).given()
                .relaxedHTTPSValidation();
    }

    @Override
    public void start() {
        super.start();

        RestAssured.baseURI = HTTP + getHost();
        RestAssured.basePath = basePath;
        RestAssured.port = getMappedPort(httpPort);

        Log.debug(this, "REST service running at " + HTTP + getHost() + ":" + getMappedPort(httpPort));
    }

    @Override
    public void stop() {
        super.stop();
        RestAssured.reset();
    }
}
