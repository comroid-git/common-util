package org.comroid.restless;

import com.google.common.flogger.FluentLogger;
import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.common.func.Provider;
import org.comroid.common.iter.Span;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.cache.Cache;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainer;
import org.comroid.varbind.container.DataContainerBase;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;

public final class REST<D> {
    public static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final HttpAdapter httpAdapter;
    private final SerializationAdapter<?, ?, ?> serializationAdapter;
    private final Ratelimiter ratelimiter;
    private final @Nullable D dependencyObject;

    public REST(
        HttpAdapter httpAdapter,
        SerializationAdapter<?, ?, ?> serializationAdapter,
        @Nullable D dependencyObject
) {
        this(httpAdapter, serializationAdapter, Ratelimiter.INSTANT, dependencyObject);
    }

    public REST(
            HttpAdapter httpAdapter,
            SerializationAdapter<?, ?, ?> serializationAdapter,
            Ratelimiter ratelimiter,
            @Nullable D dependencyObject
    ) {
        this.httpAdapter = Objects.requireNonNull(httpAdapter, "HttpAdapter");
        this.serializationAdapter = Objects.requireNonNull(serializationAdapter, "SerializationAdapter");
        this.ratelimiter = Objects.requireNonNull(ratelimiter, "Ratelimiter");
        this.dependencyObject = dependencyObject;
    }

    public Request<UniObjectNode> request() {
        return new Request<>(this, Invocable.paramReturning(UniObjectNode.class));
    }

    public <T extends DataContainer<? extends D>> Request<T> request(Class<T> type) {
        return request(DataContainerBase.findRootBind(type));
    }

    public <T extends DataContainer<? extends D>> Request<T> request(GroupBind<T, D> group) {
        return new Request<>(
                this,
                Polyfill.uncheckedCast(group.getConstructor()
                        .orElseThrow(() -> new NoSuchElementException("No constructor applied to GroupBind")))
        );
    }

    public <T> Request<T> request(Invocable<T> creator) {
        return new Request<>(this, creator);
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

        public static final class List extends ArrayList<Header> {
            public boolean add(String name, String value) {
                return super.add(new Header(name, value));
            }

            public void forEach(BiConsumer<String, String> action) {
                forEach(header -> action.accept(header.getName(), header.getValue()));
            }
        }
    }

    public static class Response {
        private final int statusCode;
        private final UniNode body;
        private final Header.List headers = new Header.List();

        public Response(int statusCode, UniNode body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public Response(REST rest, int statusCode, String body) {
            this(statusCode, rest.serializationAdapter.createUniNode(body));
        }

        public static Response empty(SerializationAdapter seriLib, @MagicConstant(valuesFromClass = HTTPStatusCodes.class) int code) {
            return new Response(code, seriLib.createUniNode(null));
        }

        public int getStatusCode() {
            return statusCode;
        }

        public UniNode getBody() {
            return body;
        }

        public Header.List getHeaders() {
            return headers;
        }
    }

    public final class Request<T> {
        private final REST rest;
        private final Header.List headers;
        private final Invocable<T> tProducer;
        private CompletableFuture<REST.Response> execution = null;
        private Provider<URL> urlProvider;
        private Method method;
        private String body;
        private int expectedCode = HTTPStatusCodes.OK;

        public Request(REST rest, Invocable<T> tProducer) {
            this.rest = rest;
            this.tProducer = tProducer;
            this.headers = new Header.List();
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

        public final Header.List getHeaders() {
            return headers;
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
            return url(Polyfill.url(spec, null));
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
            if (execution == null) {
                logger.at(Level.FINE)
                        .log("Executing request %s @ %s");
                execution = httpAdapter.call(rest, method, urlProvider, headers, serializationAdapter.getMimeType(), body);
            }

            return execution.thenApply(response -> {
                if (response.statusCode != expectedCode) {
                    logger.at(Level.WARNING)
                            .log("Unexpected Response status code %d; expected %d", response.statusCode, expectedCode);
                }

                return response;
            });
        }

        public final CompletableFuture<T> execute$deserializeSingle() {
            return execute$deserialize().thenApply(Span::requireNonNull);
        }

        public final CompletableFuture<Span<T>> execute$deserialize() {
            return execute$body().thenApply(node -> {
                switch (node.getType()) {
                    case OBJECT:
                        return Span.singleton(tProducer.autoInvoke(dependencyObject, node.asObjectNode()));
                    case ARRAY:
                        return node.asArrayNode()
                                .asNodeList()
                                .stream()
                                .map(UniNode::asObjectNode)
                                .map(sub -> tProducer.autoInvoke(dependencyObject, sub))
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

        public final <ID> CompletableFuture<Span<T>> execute$autoCache(
                VarBind<?, ? super D, ?, ID> identifyBind, Cache<ID, ? super T> cache
        ) {
            return execute$body().thenApply(node -> {
                if (node.isObjectNode()) {
                    return Span.singleton(cacheProduce(identifyBind, cache, node.asObjectNode()));
                } else if (node.isArrayNode()) {
                    return node.asNodeList()
                            .stream()
                            .map(UniNode::asObjectNode)
                            .map(obj -> cacheProduce(identifyBind, cache, obj))
                            .collect(Span.collector());
                } else {
                    throw new AssertionError();
                }
            });
        }

        private <ID> T cacheProduce(VarBind<?, ? super D, ?, ID> identifyBind, Cache<ID, ? super T> cache, UniObjectNode obj) {
            ID id = identifyBind.getFrom(obj);

            if (id == null) {
                throw new IllegalArgumentException("Invalid Data: Could not resolve identifying Bind");
            }

            if (cache.containsKey(id)) {
                //noinspection unchecked
                cache.getReference(id, false) // should be present
                        .compute(old -> (T) (
                                (DataContainer<D>) Objects.requireNonNull(old, "Assert failed: Cache did not contain object")
                        ).updateFrom(obj));
            } else {
                cache.getReference(id, true)
                        .set(tProducer.autoInvoke(dependencyObject, obj));
            }

            //noinspection unchecked
            return (T) cache.requireNonNull(id, "Assert failed: Cache is still missing key " + id);
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
    }
}
