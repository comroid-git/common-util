package org.comroid.restless;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.comroid.common.func.Provider;
import org.comroid.restless.socket.WebSocket;
import org.comroid.uniform.SerializationAdapter;

public interface HttpAdapter {
    static HttpAdapter autodetect() {
        throw new UnsupportedOperationException();
    }

    <O> CompletableFuture<WebSocket<O>> createWebSocket(
            SerializationAdapter<?, ?, ?> seriLib,
            WebSocket.Header.List headers,
            Executor executor,
            URI uri,
            Function<String, O> preprocessor
    );

    CompletableFuture<REST.Response> call(
            REST rest,
            REST.Method method,
            Provider<URL> urlProvider,
            Collection<REST.Header> headers,
            String mimeType,
            String body
    );
}
