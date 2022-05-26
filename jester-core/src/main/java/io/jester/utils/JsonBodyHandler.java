package io.jester.utils;

import static io.jester.utils.JsonUtils.fromJson;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

import io.jester.logging.Log;

public class JsonBodyHandler<W> implements HttpResponse.BodyHandler<Supplier<W>> {

    private final Class<W> wClass;

    public JsonBodyHandler(Class<W> wClass) {
        this.wClass = wClass;
    }

    @Override
    public HttpResponse.BodySubscriber<Supplier<W>> apply(HttpResponse.ResponseInfo responseInfo) {
        return asJson(wClass);
    }

    private static <W> HttpResponse.BodySubscriber<Supplier<W>> asJson(Class<W> targetType) {
        HttpResponse.BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();

        return HttpResponse.BodySubscribers.mapping(upstream, inputStream -> toSupplierOfType(inputStream, targetType));
    }

    private static <W> Supplier<W> toSupplierOfType(InputStream inputStream, Class<W> targetType) {
        return () -> {
            try (InputStream stream = inputStream) {
                return fromJson(stream, targetType);
            } catch (IOException ex) {
                Log.warn("Exception reading response in HttpService", ex);
                return null;
            }
        };
    }
}
