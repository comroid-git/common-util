package org.comroid.restless;

import com.google.common.flogger.FluentLogger;
import com.sun.net.httpserver.Headers;
import org.comroid.api.Polyfill;
import org.comroid.api.Invocable;
import org.comroid.mutatio.Span;
import org.comroid.restless.endpoint.AccessibleEndpoint;
import org.comroid.restless.endpoint.CompleteEndpoint;
import org.comroid.restless.endpoint.RatelimitedEndpoint;
import org.comroid.restless.server.Ratelimiter;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
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
    private final Executor executor;

    public HttpAdapter getHttpAdapter() {
        return httpAdapter;
    }

    public SerializationAdapter<?, ?, ?> getSerializationAdapter() {
        return serializationAdapter;
    }

    public Ratelimiter getRatelimiter() {
        return ratelimiter;
    }

    public Optional<D> getDependencyObject() {
        return Optional.ofNullable(dependencyObject);
    }

    public final Executor getExecutor() {
        return executor;
    }

    public REST(
            HttpAdapter httpAdapter,
            SerializationAdapter<?, ?, ?> serializationAdapter
    ) {
        this(httpAdapter, serializationAdapter, ForkJoinPool.commonPool(), null);
    }

    public REST(
            HttpAdapter httpAdapter,
            SerializationAdapter<?, ?, ?> serializationAdapter,
            @Nullable D dependencyObject
    ) {
        this(httpAdapter, serializationAdapter, ForkJoinPool.commonPool(), dependencyObject);
    }

    public REST(
            HttpAdapter httpAdapter,
            SerializationAdapter<?, ?, ?> serializationAdapter,
            Executor requestExecutor,
            @Nullable D dependencyObject
    ) {
        this(
                httpAdapter,
                serializationAdapter,
                requestExecutor,
                Ratelimiter.INSTANT,
                dependencyObject
        );
    }

    public REST(
            HttpAdapter httpAdapter,
            SerializationAdapter<?, ?, ?> serializationAdapter,
            ScheduledExecutorService scheduledExecutorService,
            @Nullable D dependencyObject,
            RatelimitedEndpoint... pool
    ) {
        this(
                httpAdapter,
                serializationAdapter,
                scheduledExecutorService,
                Ratelimiter.ofPool(scheduledExecutorService, pool),
                dependencyObject
        );
    }

    public REST(
            HttpAdapter httpAdapter,
            SerializationAdapter<?, ?, ?> serializationAdapter,
            Executor requestExecutor,
            Ratelimiter ratelimiter,
            @Nullable D dependencyObject
    ) {
        this.httpAdapter = Objects.requireNonNull(httpAdapter, "HttpAdapter");
        this.serializationAdapter = Objects.requireNonNull(serializationAdapter, "SerializationAdapter");
        this.executor = Objects.requireNonNull(requestExecutor, "RequestExecutor");
        this.ratelimiter = Objects.requireNonNull(ratelimiter, "Ratelimiter");
        this.dependencyObject = dependencyObject;
    }

    public Request<UniObjectNode> request() {
        return new Request<>(Invocable.paramReturning(UniObjectNode.class));
    }

    public <T extends DataContainer<? extends D>> Request<T> request(Class<T> type) {
        return request(DataContainerBase.findRootBind(type));
    }

    public <T extends DataContainer<? extends D>> Request<T> request(GroupBind<T, D> group) {
        //noinspection unchecked
        return request((Invocable<T>) Polyfill.uncheckedCast(group.getConstructor()
                .orElseThrow(() -> new NoSuchElementException("No constructor applied to GroupBind"))));
    }

    public <T> Request<T> request(Invocable<T> creator) {
        return new Request<>(creator);
    }

    public enum Method {
        GET,

        PUT,

        POST,

        PATCH,

        DELETE,

        HEAD;

        @Override
        public String toString() {
            return name();
        }
    }

    public static final class Header {
        private final String name;
        private final String value;

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static final class List extends ArrayList<Header> {
            public static List of(Headers headers) {
                final List list = new List();

                headers.forEach((name, values) -> list
                        .add(name, values.size() == 1
                                ? values.get(0)
                                : Arrays.toString(values.toArray())));

                return list;
            }

            public boolean add(String name, String value) {
                return super.add(new Header(name, value));
            }

            public boolean contains(String name) {
                return stream().anyMatch(it -> it.name.equals(name));
            }

            public String get(String key) {
                return stream()
                        .filter(it -> it.name.equals(key))
                        .findAny()
                        .map(Header::getValue)
                        .orElse(null);
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

        public int getStatusCode() {
            return statusCode;
        }

        public UniNode getBody() {
            return body;
        }

        public Header.List getHeaders() {
            return headers;
        }

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
    }

    public final class Request<T> {
        private final Header.List headers;
        private final Invocable<T> tProducer;
        private final CompletableFuture<REST.Response> execution = new CompletableFuture<>();
        private CompleteEndpoint endpoint;
        private Method method;
        private String body;
        private int expectedCode = HTTPStatusCodes.OK;

        public final CompleteEndpoint getEndpoint() {
            return endpoint;
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

        public REST<D> getREST() {
            return REST.this;
        }

        public Request(Invocable<T> tProducer) {
            this.tProducer = tProducer;
            this.headers = new Header.List();
        }

        @Override
        public String toString() {
            return String.format("%s @ %s", method.name(), endpoint.getSpec());
        }

        public final Request<T> expect(@MagicConstant(valuesFromClass = HTTPStatusCodes.class) int code) {
            this.expectedCode = code;

            return this;
        }

        public final Request<T> endpoint(CompleteEndpoint endpoint) {
            this.endpoint = endpoint;

            return this;
        }

        public final Request<T> endpoint(AccessibleEndpoint endpoint, Object... args) {
            return endpoint(endpoint.complete(args));
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

        public final synchronized CompletableFuture<REST.Response> execute() {
            if (!execution.isDone()) {
                logger.at(Level.FINE)
                        .log("Executing request %s @ %s");
                getREST().ratelimiter.apply(endpoint.getEndpoint(), this)
                        .thenCompose(request -> httpAdapter.call(request, serializationAdapter.getMimeType()))
                        .thenAcceptAsync(response -> {
                            if (response.statusCode != expectedCode) {
                                logger.at(Level.WARNING)
                                        .log("Unexpected Response status code %d; expected %d", response.statusCode, expectedCode);
                            }

                            execution.complete(response);
                        }, executor);
            }

            return execution;
        }

        public final CompletableFuture<Integer> execute$statusCode() {
            return execute().thenApply(Response::getStatusCode);
        }

        public final CompletableFuture<UniNode> execute$body() {
            return execute().thenApply(Response::getBody);
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

        public final CompletableFuture<T> execute$deserializeSingle() {
            return execute$deserialize().thenApply(Span::requireNonNull);
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
    }
}
