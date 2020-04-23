package org.comroid.restless;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.comroid.common.func.Provider;
import org.comroid.restless.socket.WebSocket;

public interface HttpAdapter {
    WebSocket createWebSocket(Executor executor, URI uri);

    CompletableFuture<REST.Response> call(
            REST rest,
            REST.Method method,
            Provider<URL> urlProvider,
            Collection<REST.Header> headers,
            String mimeType,
            String body
    );
}
