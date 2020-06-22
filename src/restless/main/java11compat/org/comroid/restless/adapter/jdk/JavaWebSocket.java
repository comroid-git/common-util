package org.comroid.restless.adapter.jdk;

import org.comroid.api.Polyfill;
import org.comroid.api.UUIDContainer;
import org.comroid.listnr.ListnrCore;
import org.comroid.mutatio.ref.Reference;
import org.comroid.restless.REST;
import org.comroid.restless.socket.WebSocketData;
import org.comroid.restless.socket.event.WebSocketEvent;
import org.comroid.restless.socket.event.WebSocketPayload;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniNode;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

import static java.lang.System.currentTimeMillis;

public final class JavaWebSocket extends UUIDContainer implements org.comroid.restless.socket.WebSocket {
    private final WebSocket.Listener listener = new JListener();
    private final SerializationAdapter<?, ?, ?> seriLib;
    private final Reference<WebSocket> jSocketRef;
    private final ListnrCore listnrCore;

    private JavaWebSocket(SerializationAdapter<?, ?, ?> seriLib, CompletableFuture<WebSocket> socketFuture, Executor executor) {
        this.seriLib = seriLib;
        this.jSocketRef = Reference.later(socketFuture);
        this.listnrCore = new ListnrCore(executor);
    }

    public static CompletableFuture<JavaWebSocket> create(
            SerializationAdapter<?, ?, ?> seriLib,
            Executor executor,
            HttpClient httpClient,
            URI uri,
            REST.Header.List headers
    ) {
        final WebSocket.Builder builder = httpClient.newWebSocketBuilder();

        headers.forEach(builder::header);

        final CompletableFuture<WebSocket> jSocketFuture = new CompletableFuture<>();
        final JavaWebSocket socket = new JavaWebSocket(seriLib, jSocketFuture, executor);

        return builder.buildAsync(uri, socket.listener)
                .thenApply(jSocketFuture::complete)
                .thenApply(nil -> socket);
    }

    @Override
    public ListnrCore listnr() {
        return listnrCore;
    }

    @Override
    public CompletableFuture<?> sendData(UniNode node) {
        final String str = node.toString();
        final int len = str.length();

        if (len <= MAX_MESSAGE_LENGTH)
            return jSocketRef.requireNonNull("WebSocket missing")
                    .sendText(str, true);

        return CompletableFuture.allOf(
                IntStream.range(0, (len / MAX_MESSAGE_LENGTH) + 1)
                        .sequential()
                        .map(x -> MAX_MESSAGE_LENGTH * x)
                        .mapToObj(start -> (start + MAX_MESSAGE_LENGTH) > str.length()
                                ? str.substring(start)
                                : str.substring(start, start + MAX_MESSAGE_LENGTH))
                        .map(data -> jSocketRef.requireNonNull("WebSocket missing")
                                .sendText(data, (data.length() != MAX_MESSAGE_LENGTH)))
                        .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<?> sendClose(int statusCode, String reason) {
        return jSocketRef.requireNonNull("WebSocket missing")
                .sendClose(statusCode, reason);
    }

    @Override
    public CompletableFuture<Long> evaluatePing() {
        final CompletableFuture<WebSocketPayload.Pong> nextPong = eventPipe(WebSocketEvent.PONG).next();
        return jSocketRef.requireNonNull("WebSocket missing")
                .sendPing(ByteBuffer.allocate(0))
                .thenApply(nil -> currentTimeMillis())
                .thenCombine(nextPong, (started, payload) -> currentTimeMillis() - started);
    }

    private final class JListener implements WebSocket.Listener {
        private StringBuilder sb = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            publish(WebSocketEvent.OPEN, WebSocketData.empty(JavaWebSocket.this, WebSocketEvent.OPEN));
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            handleData(webSocket, last, data.toString().intern());
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            handleData(webSocket, last, new String(data.array()).intern());
            return null;
        }

        @Override
        public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
            publish(WebSocketEvent.PING, WebSocketData.empty(JavaWebSocket.this, WebSocketEvent.PING));
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
            publish(WebSocketEvent.PONG, WebSocketData.empty(JavaWebSocket.this, WebSocketEvent.PONG));
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            publish(WebSocketEvent.ERROR, WebSocketData.error(JavaWebSocket.this, error));
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            publish(WebSocketEvent.CLOSE, WebSocketData.close(JavaWebSocket.this, statusCode, reason));
            return Polyfill.infiniteFuture();
        }

        @Nullable
        private void handleData(WebSocket webSocket, boolean last, String newData) {
            sb.append(newData);

            if (last) {
                final UniNode node = seriLib.createUniNode(sb.toString());
                publish(WebSocketEvent.DATA, WebSocketData.ofNode(JavaWebSocket.this, node));
                sb = new StringBuilder();
            }

            webSocket.request(1);
        }
    }
}
