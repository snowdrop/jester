package io.jcloud.api;

import io.jcloud.core.BaseService;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class RestService extends BaseService<RestService> {

    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final String HTTP = "http://";
    private static final String BASE_PATH = "/";

    private final int httpPort;
    private final String basePath;

    public RestService() {
        this(DEFAULT_HTTP_PORT, BASE_PATH);
    }

    public RestService(int httpPort, String basePath) {
        this.httpPort = httpPort;
        this.basePath = basePath;
    }

    public RequestSpecification given() {
        return RestAssured.given().baseUri(HTTP + getHost()).basePath(basePath).port(getMappedPort(httpPort));
    }

    @Override
    public void start() {
        super.start();

        RestAssured.baseURI = HTTP + getHost();
        RestAssured.basePath = basePath;
        RestAssured.port = getMappedPort(httpPort);
    }

    @Override
    public void stop() {
        super.stop();
        RestAssured.reset();
    }
}
