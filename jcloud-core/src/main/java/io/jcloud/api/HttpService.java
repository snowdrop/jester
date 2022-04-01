package io.jcloud.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.jcloud.core.BaseService;

public class HttpService extends BaseService<HttpService> {

    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final String HTTP = "http://";
    private static final String BASE_PATH = "/";

    protected final int httpPort;
    protected final String basePath;

    private HttpClient innerHttpClient;
    private ExecutorService executorService;
    private Integer concurrentCalls;

    public HttpService() {
        this(DEFAULT_HTTP_PORT, BASE_PATH);
    }

    public HttpService(int httpPort, String basePath) {
        this.httpPort = httpPort;
        this.basePath = basePath;
    }

    public HttpService withHttpClient(HttpClient httpClient) {
        closeHttpClient();
        innerHttpClient = httpClient;
        return this;
    }

    public HttpService withConcurrentCalls(int concurrentCalls) {
        this.concurrentCalls = concurrentCalls;
        return this;
    }

    public HttpResponse<String> getString(String... paths) {
        createHttpClientIfNull();
        try {
            return innerHttpClient.send(HttpRequest.newBuilder(target(paths)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        super.stop();

        closeHttpClient();
    }

    private void closeHttpClient() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }

            executorService = null;
        }
    }

    private void createHttpClientIfNull() {
        if (innerHttpClient == null) {
            HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
            if (concurrentCalls != null) {
                executorService = Executors.newFixedThreadPool(concurrentCalls);
                httpClientBuilder.executor(executorService);
            }

            innerHttpClient = httpClientBuilder.build();
        }
    }

    private URI target(String[] paths) {
        return URI.create(String.format("%s%s:%s%s", HTTP, getHost(), getMappedPort(httpPort),
                Stream.of(paths).collect(Collectors.joining("/"))));
    }
}
