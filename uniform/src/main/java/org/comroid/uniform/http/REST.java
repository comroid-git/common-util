package org.comroid.uniform.http;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import org.comroid.common.iter.Span;
import org.comroid.uniform.data.SerializationAdapter;
import org.comroid.uniform.data.node.UniNode;
import org.comroid.varbind.GroupBind;
import org.comroid.varbind.VarBind;

import org.jetbrains.annotations.Nullable;

public final class REST {
    private final HttpAdapter httpAdapter;
    private final SerializationAdapter<Object, Object, Object> serializationAdapter;

    public REST(HttpAdapter httpAdapter, SerializationAdapter<Object, Object, Object> serializationAdapter) {
        this.httpAdapter = Objects.requireNonNull(httpAdapter, "HttpAdapter");
        this.serializationAdapter = Objects.requireNonNull(serializationAdapter, "SerializationAdapter");
    }

    public Request request(URL url) {
        return new Request(this, url);
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
        private final UniNode body;

        public Response(REST rest, int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = rest.serializationAdapter.createUniNode(body);
        }

        public int getStatusCode() {
            return statusCode;
        }

        public UniNode getBody() {
            return body;
        }
    }

    public final class Request<T> {
        private final REST rest;
        private final URL url;
        private final Collection<Header> headers;
        private final @Nullable GroupBind
        private CompletableFuture<REST.Response> execution = null;
        private Method method;
        private String body;

        public Request(REST rest, URL url) {
            this.rest = rest;
            this.url = url;
            this.headers = new ArrayList<>();
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

        public CompletableFuture<REST.Response> execute() {
            return execution == null ? (execution = httpAdapter.call(
                    rest, method, url, headers, serializationAdapter.getMimeType(), body)) : execution;
        }

        public CompletableFuture<Integer> execute$statusCode() {
            return execute().thenApply(Response::getStatusCode);
        }

        public CompletableFuture<UniNode> execute$body() {
            return execute().thenApply(Response::getBody);
        }

        public CompletableFuture<Span<T>> execute$deserialize(Bind) {
            return execute$body().thenApply(dataAdapter::deserialize);
        }

        public <R> CompletableFuture<Span<R>> execute$map(Function<UniNode, R> remapper) {
            return execute$deserialize().thenApply(span -> span.stream()
                    .map(remapper)
                    .collect(Span.<R>make().collector()));
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
