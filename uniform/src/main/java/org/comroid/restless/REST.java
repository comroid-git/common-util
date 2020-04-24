package org.comroid.restless;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Provider;
import org.comroid.common.iter.Span;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.VarCarrier;
import org.comroid.varbind.VariableCarrier;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;

public final class REST<D> {
    public REST(
            HttpAdapter httpAdapter, @Nullable D dependencyObject, SerializationAdapter<?, ?, ?> serializationAdapter
    ) {
        this.httpAdapter          = Objects.requireNonNull(httpAdapter, "HttpAdapter");
        this.dependencyObject     = dependencyObject;
        this.serializationAdapter = Objects.requireNonNull(serializationAdapter, "SerializationAdapter");
    }

    public <T extends VarCarrier<D>> Request<T> request(Class<T> type) {
        return new Request<>(this,
                VariableCarrier.findRootBind(type)
                        .autoConstructor(type, (Class<D>) (
                                dependencyObject == null ? Object.class : dependencyObject.getClass()
                        ))
        );
    }

    public Request<UniObjectNode> request() {
        return new Request<>(this, (dep, node) -> node);
    }

    public static final class Header {
        public Header(String name, String value) {
            this.name  = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
        private final String name;
        private final String value;
    }

    public static final class Response {
        public Response(REST rest, int statusCode, String body) {
            this.statusCode = statusCode;
            this.body       = rest.serializationAdapter.createUniNode(body);
        }

        public int getStatusCode() {
            return statusCode;
        }

        public UniNode getBody() {
            return body;
        }
        private final int     statusCode;
        private final UniNode body;
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

    public final class Request<T> {
        public Request(REST rest, @Nullable BiFunction<D, UniObjectNode, T> tProducer) {
            this.rest      = rest;
            this.tProducer = tProducer;
            this.headers   = new ArrayList<>();
        }

        public final Provider<URL> getUrlProvider() {
            return urlProvider;
        }

        public final URL getUrl() {
            return urlProvider.now();
        }

        public final Method getMethod() {
            return method;
        }

        public final String getBody() {
            return body;
        }

        public final Collection<Header> getHeaders() {
            return Collections.unmodifiableCollection(headers);
        }

        public final Request<T> expect(@MagicConstant(valuesFromClass = HTTPStatusCodes.class) int code) {
            this.expectedCode = code;

            return this;
        }

        public final Request<T> url(Provider<URL> urlProvider) {
            this.urlProvider = urlProvider;

            return this;
        }

        public final Request<T> url(String spec) throws AssertionError {
            return url(Polyfill.url(spec));
        }

        public final Request<T> url(URL url) {
            this.urlProvider = Provider.constant(url);

            return this;
        }

        public final Request<T> method(REST.Method method) {
            this.method = method;

            return this;
        }

        public final Request<T> body(String body) {
            this.body = body;

            return this;
        }

        public final Request<T> addHeader(String name, String value) {
            this.headers.add(new Header(name, value));

            return this;
        }

        public final boolean removeHeaders(Predicate<Header> filter) {
            return headers.removeIf(filter);
        }

        public final CompletableFuture<Integer> execute$statusCode() {
            return execute().thenApply(Response::getStatusCode);
        }

        public final CompletableFuture<REST.Response> execute() {
            return (
                    execution == null ? (
                            execution = httpAdapter.call(rest,
                                    method,
                                    urlProvider,
                                    headers,
                                    serializationAdapter.getMimeType(),
                                    body
                            )
                    ) : execution
            );
        }

        public final CompletableFuture<T> execute$deserializeSingle() {
            return execute$deserialize().thenApply(Span::requireNonNull);
        }

        public final CompletableFuture<Span<T>> execute$deserialize() {
            return execute$body().thenApply(node -> {
                switch (node.getType()) {
                    case OBJECT:
                        return Span.singleton(tProducer.apply(dependencyObject, node.asObjectNode()));
                    case ARRAY:
                        return node.asArrayNode()
                                .asNodeList()
                                .stream()
                                .map(UniNode::asObjectNode)
                                .map(sub -> tProducer.apply(dependencyObject, sub))
                                .collect(Span.collector());
                    case VALUE:
                        throw new AssertionError("Cannot deserialize from UniValueNode");
                }

                throw new AssertionError();
            });
        }

        public final CompletableFuture<UniNode> execute$body() {
            return execute().thenApply(Response::getBody);
        }

        public final <R> CompletableFuture<Span<R>> execute$map(Function<T, R> remapper) {
            return execute$deserialize().thenApply(span -> span.stream()
                    .map(remapper)
                    .collect(Span.collector()));
        }

        public final <R> CompletableFuture<R> execute$mapSingle(Function<T, R> remapper) {
            return execute$deserialize().thenApply(span -> {
                if (!span.isSingle()) {
                    throw new IllegalArgumentException("Span too large");
                }

                return remapper.apply(span.get());
            });
        }
        private final           REST                             rest;
        private final           Collection<Header>               headers;
        private final @Nullable BiFunction<D, UniObjectNode, T>  tProducer;
        private                 CompletableFuture<REST.Response> execution    = null;
        private                 Provider<URL>                    urlProvider;
        private                 Method                           method;
        private                 String                           body;
        private                 int                              expectedCode = HTTPStatusCodes.OK;
    }
    private final           HttpAdapter                   httpAdapter;
    private final @Nullable D                             dependencyObject;
    private final           SerializationAdapter<?, ?, ?> serializationAdapter;
}
