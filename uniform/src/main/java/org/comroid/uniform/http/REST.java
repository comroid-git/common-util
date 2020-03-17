package org.comroid.uniform.http;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.comroid.common.iter.Span;
import org.comroid.uniform.data.DataConverter;

public final class REST<T> {
    private final static Map<Class<?>, REST<?>> cache = new ConcurrentHashMap<>();

    public static <T> REST<T> getOrCreate(Class<T> forClass, HttpAdapter httpAdapter, DataConverter<T, ?, ?, ?> dataAdapter) {
        //noinspection unchecked
        return (REST<T>) cache.computeIfAbsent(forClass, key -> new REST<>(httpAdapter, dataAdapter));
    }

    public static <T> Optional<REST<T>> get(Class<T> forClass) {
        //noinspection unchecked
        return Optional.ofNullable((REST<T>) cache.getOrDefault(forClass, null));
    }

    private final HttpAdapter httpAdapter;
    private final DataConverter<T, ?, ?, ?> dataAdapter;

    private REST(HttpAdapter httpAdapter, DataConverter<T, ?, ?, ?> dataAdapter) {
        this.httpAdapter = Objects.requireNonNull(httpAdapter, "HttpAdapter");
        this.dataAdapter = Objects.requireNonNull(dataAdapter, "DataAdapter");
    }

    public Request request(URL url) {
        return new Request(url);
    }

    public final class Request {
        private CompletableFuture<REST.Response> execution = null;
        private URL url;
        private Method method;
        private String body;
        private Collection<Header> headers;

        public Request(URL url) {
            this.url = url;
            this.headers = new ArrayList<>();
        }

        public Request method(REST.Method method) {
            this.method = method;

            return this;
        }

        public Request body(String body) {
            this.body = body;

            return this;
        }

        public Request addHeader(String name, String value) {
            this.headers.add(new Header(name, value));

            return this;
        }

        public boolean removeHeaders(Predicate<Header> filter) {
            return headers.removeIf(filter);
        }

        public URL getUrl() {
            return url;
        }

        public Method getMethod() {
            return method;
        }

        public String getBody() {
            return body;
        }

        public Collection<Header> getHeaders() {
            return Collections.unmodifiableCollection(headers);
        }

        public CompletableFuture<REST.Response> execute() {
            return execution == null
                    ? (execution = httpAdapter.call(method, url, headers, dataAdapter.mimeType, body))
                    : execution;
        }

        public CompletableFuture<Integer> execute$statusCode() {
            return execute().thenApply(Response::getStatusCode);
        }

        public CompletableFuture<String> execute$body() {
            return execute().thenApply(Response::getBody);
        }

        public CompletableFuture<Span<T>> execute$deserialize() {
            return execute$body().thenApply(dataAdapter::deserialize);
        }

        public <R> CompletableFuture<Span<R>> execute$map(Function<T, R> remapper) {
            return execute$deserialize().thenApply(span -> span.stream()
                    .map(remapper)
                    .collect(Span.collector(false)));
        }
    }

    public static final class Header {
        private final String name;
        private final String value;

        public Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    public static final class Response {
        private final int statusCode;
        private final String body;

        public Response(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }
    }

    public enum Method {
        GET,

        PUT,

        POST,

        PATCH,

        DELETE;

        @Override
        public String toString() {
            return name();
        }
    }
}
