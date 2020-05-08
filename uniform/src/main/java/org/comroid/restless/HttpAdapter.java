package org.comroid.restless;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.comroid.common.func.Provider;
import org.comroid.restless.socket.WebSocket;
import org.comroid.restless.socket.WebSocketEvent;
import org.comroid.uniform.SerializationAdapter;

@SuppressWarnings("rawtypes")
public interface HttpAdapter {
    static HttpAdapter autodetect() {
        throw new UnsupportedOperationException();
    }

    <O, E extends WebSocketEvent<? super E>> CompletableFuture<WebSocket<O, E>> createWebSocket(
            SerializationAdapter<?, ?, ?> seriLib,
            WebSocket.Header.List headers,
            Executor executor,
            URI uri,
            Function<String, O> preprocessor
    );

    @Deprecated
    CompletableFuture<REST.Response> call(
            REST rest,
            REST.Method method,
            Provider<URL> urlProvider,
            Collection<REST.Header> headers,
            String mimeType,
            String body
    );

    CompletableFuture<REST.Response> call(REST rest, String mimeType, REST.Request request);
}
