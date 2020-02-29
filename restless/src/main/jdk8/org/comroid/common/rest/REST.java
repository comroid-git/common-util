package org.comroid.common.rest;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.comroid.common.rest.uniform.Adapter;

import com.google.common.flogger.FluentLogger;

public final class REST {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final Map<Adapter, REST> cache = new ConcurrentHashMap<>();

    public static <DAT, TYP> Request.Builder<DAT, TYP> request(Adapter adapter) {
        return request(adapter, Context.DUMMY);
    }

    public static <DAT, TYP> REST.Request.Builder<DAT, TYP> request(Adapter adapter, Context context) {
        return get(adapter).request(context);
    }

    public static REST get(Adapter adapter) {
        return cache.computeIfAbsent(adapter, REST::new);
    }

    public final Adapter<?, ?, ?, ?> adapter;

    private REST(Adapter adapter) {
        this.adapter = adapter;
    }

    public <DAT, TYP> Request.Builder<DAT, TYP> request() {
        return request(Context.DUMMY);
    }

    public <DAT, TYP> REST.Request.Builder<DAT, TYP> request(Context context) {
        return new Request.Builder<>(this, context);
    }

    public interface Context {
        Context DUMMY = new Context() {
            @Override
            public <T, B> Request<T, B> finalize(Request.Builder<T, B> builder) {
                return builder.build();
            }
        };

        <T, B> REST.Request<T, B> finalize(REST.Request.Builder<T, B> builder);
    }

    public static class Request<TYP, DAT> {
        private final CompletableFuture<REST.Response> execution;
        private Adapter<?, ?, DAT, TYP> adapter;
        private Context context;

        private Request(CompletableFuture<REST.Response> execution) {
            this.execution = Objects.requireNonNull(execution, "Execution cannot be null");
        }

        public Adapter<?, ?, DAT, TYP> getAdapter() {
            return adapter;
        }

        public REST.Context getContext() {
            return context;
        }

        public CompletableFuture<REST.Response> execute() {
            return execution;
        }

        public CompletableFuture<Integer> execute$statusCode() {
            return execute().thenApply(Response::getResponseCode);
        }

        public CompletableFuture<DAT> execute$parse() {
            return execute().thenApply(response -> getAdapter().getSerializer().parse(response.getRequestBody().toString()));
        }

        public CompletableFuture<TYP> execute$deserialize() {
            return execute$parse().thenApply(data -> getAdapter().getSerializer().deserialize(data));
        }

        public static class Builder<TYP, DAT> {
            protected final REST rest;
            protected final REST.Context context;
            protected final List<REST.Request.Builder.Header> headers = new ArrayList<>();
            protected URL url = null;
            protected REST.Request.Action action = REST.Request.Action.GET;

            protected Builder(REST rest) {
                this(rest, Context.DUMMY);
            }

            private Builder(REST rest, Context context) {
                this.rest = rest;
                this.context = context;
            }

            public Context getContext() {
                return context;
            }

            public URL getUrl() {
                return url;
            }

            public Builder<TYP, DAT> setUrl(URL url) {
                this.url = url;

                return this;
            }

            public REST.Request.Action getAction() {
                return action;
            }

            public Builder<TYP, DAT> setAction(REST.Method method, REST.Request.Body body) {
                this.action = Action.of(method, body);

                return this;
            }

            public List<Request.Header> getHeaders() {
                return Collections.unmodifiableList(headers);
            }

            public void updateHeaderIf(Predicate<Request.Header> filter, Consumer<Request.Header> updater) {
                headers.stream()
                        .filter(filter)
                        .forEachOrdered(updater);
            }

            public boolean removeHeaderIf(Predicate<Request.Header> filter) {
                return headers.removeIf(filter);
            }

            public Request<TYP, DAT> build() {
                Objects.requireNonNull(url, "URL cannot be null");

                return new Request<>(rest.adapter.call(this));
            }

            static class Header extends Request.Header {
                public Header setName(String name) {
                    super.name = name;

                    return this;
                }

                public Header setValue(String value) {
                    super.value = value;

                    return this;
                }
            }

            static class Body extends Request.Body {
                public Body(CharSequence seq) {
                    super();

                    buffer.append(seq);
                }
            }
        }

        public static abstract class Header {
            protected String name = null;
            protected String value = null;

            public Header() {
            }

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

        public static abstract class Body {
            public static Body of(CharSequence seq) {
                return new Builder.Body(seq);
            }

            protected StringBuffer buffer = new StringBuffer();

            public StringBuffer getBuffer() {
                return buffer;
            }

            @Override
            public String toString() {
                return buffer.toString();
            }
        }

        public static final class Action {
            @SuppressWarnings("ConstantConditions")
            public static final Action GET = new Action(REST.Method.GET, null);

            public static REST.Request.Action of(Method method, Body body) {
                if (method == Method.GET)
                    return GET;

                return new Action(method, Objects.requireNonNull(body, "Body cannot be null"));
            }

            private final Method method;
            private final Body body;

            private Action(Method method, Body body) {
                this.method = method;
                this.body = body;
            }

            public REST.Method getMethod() {
                return method;
            }

            public REST.Request.Body getBody() {
                return body;
            }
        }
    }

    public static final class Response {
        private final REST.Request.Body requestBody;
        private final int responseCode;

        public Response(Request.Body requestBody, int responseCode) {
            this.requestBody = requestBody;
            this.responseCode = responseCode;
        }

        public Request.Body getRequestBody() {
            return requestBody;
        }

        public int getResponseCode() {
            return responseCode;
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
