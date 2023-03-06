package io.github.jester.api;

import static org.apache.http.entity.mime.MIME.CONTENT_TYPE;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.entity.ContentType;

import io.github.jester.core.BaseService;
import io.github.jester.utils.JsonBodyHandler;
import io.github.jester.utils.JsonUtils;

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

    public HttpResponse<Supplier<Map>> getAsJson(String... paths) {
        return getAsJson(Map.class, paths);
    }

    public <T> HttpResponse<Supplier<T>> getAsJson(Class<T> clazz, String... paths) {
        createHttpClientIfNull();
        try {
            return innerHttpClient.send(HttpRequest.newBuilder(target(paths)).GET().build(),
                    new JsonBodyHandler<>(clazz));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<Supplier<Map>> postAsJson(Object request, String... paths) {
        return postAsJson(request, Map.class, paths);
    }

    public <T> HttpResponse<Supplier<T>> postAsJson(Object request, Class<T> clazz, String... paths) {
        createHttpClientIfNull();
        try {
            return innerHttpClient.send(
                    HttpRequest.newBuilder(target(paths))
                            .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(request)))
                            .setHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()).build(),
                    new JsonBodyHandler<>(clazz));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <R> HttpResponse<InputStream> delete(String... paths) {
        createHttpClientIfNull();
        try {
            return innerHttpClient.send(HttpRequest.newBuilder(target(paths)).DELETE().build(),
                    HttpResponse.BodyHandlers.ofInputStream());
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
        String url = String.format("%s%s:%s%s", HTTP, getHost(), getMappedPort(httpPort),
                Stream.of(paths).collect(Collectors.joining("/")));
        return URI.create(url);
    }
}
