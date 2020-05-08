package org.comroid.uniform.adapter.http.jdk;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket.Builder;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.comroid.common.func.Provider;
import org.comroid.common.util.Bitmask;
import org.comroid.restless.HttpAdapter;
import org.comroid.restless.REST;
import org.comroid.restless.socket.WebSocket;
import org.comroid.restless.socket.WebSocketEvent;
import org.comroid.uniform.SerializationAdapter;

public final class JavaHttpAdapter implements HttpAdapter {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public <O, E extends WebSocketEvent<? super  E>> CompletableFuture<WebSocket<O, E>> createWebSocket(
            SerializationAdapter<?, ?, ?> seriLib,
            WebSocket.Header.List headers,
            Executor executor,
            URI uri,
            Function<String, O> preprocessor
    ) {
        final Builder webSocketBuilder = httpClient.newWebSocketBuilder();
        headers.forEach(header -> webSocketBuilder.header(header.getName(), header.getValue()));
        final JavaWebSocket<O, E> javaWebSocket = new JavaWebSocket<>(new ThreadGroup(String.format("%s" + "-0x%s",
                toString(),
                Integer.toHexString(Bitmask.nextFlag())
        )), preprocessor);

        return webSocketBuilder.buildAsync(uri, javaWebSocket.javaListener)
                .thenApply(socket -> {
                    javaWebSocket.socket.complete(socket);
                    return javaWebSocket;
                });
    }

    @Override
    public CompletableFuture<REST.Response> call(REST.Request request, String mimeType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final HttpRequest.Builder builder = HttpRequest.newBuilder(request.getEndpoint().getURI())
                        .method(request.getMethod().toString(), (
                                request.getBody() == null && request.getMethod() == REST.Method.GET
                                        ? HttpRequest.BodyPublishers.noBody()
                                        : HttpRequest.BodyPublishers.ofString(Objects.requireNonNull(request.getBody(), "Body cannot be null"))
                        ))
                        .header("Content-Type", mimeType);

                request.getHeaders().forEach(header -> builder.header(header.getName(), header.getValue()));

                final HttpRequest          jRequest  = builder.build();
                final HttpResponse<String> response = httpClient.send(jRequest, HttpResponse.BodyHandlers.ofString());

                return new REST.Response(request.getREST(), response.statusCode(), response.body());
            } catch (Throwable e) {
                throw new RuntimeException("Request failed", e);
            }
        });
    }
}
