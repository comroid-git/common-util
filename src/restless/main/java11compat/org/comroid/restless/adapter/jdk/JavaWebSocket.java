package org.comroid.restless.adapter.jdk;

import org.comroid.api.Polyfill;
import org.comroid.api.UUIDContainer;
import org.comroid.listnr.ListnrCore;
import org.comroid.restless.REST;
import org.comroid.restless.socket.WebSocketData;
import org.comroid.restless.socket.event.WebSocketEvent.Type;
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

public final class JavaWebSocket extends UUIDContainer implements org.comroid.restless.socket.WebSocket {
    private final WebSocket.Listener listener = new JListener();
    private final SerializationAdapter<?, ?, ?> seriLib;
    private final ListnrCore listnrCore;

    public JavaWebSocket(SerializationAdapter<?, ?, ?> seriLib, Executor executor) {
        this.seriLib = seriLib;
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

        final JavaWebSocket socket = new JavaWebSocket(seriLib, executor);

        return builder.buildAsync(uri, socket.listener)
                .thenApply(nil -> socket);
    }

    @Override
    public ListnrCore listnr() {
        return listnrCore;
    }

    private final class JListener implements WebSocket.Listener {
        private StringBuilder sb = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            JavaWebSocket.this.publish(Type.OPEN, WebSocketData.empty(JavaWebSocket.this, Type.OPEN));
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
            publish(Type.PING, WebSocketData.empty(JavaWebSocket.this, Type.PING));
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
            publish(Type.PONG, WebSocketData.empty(JavaWebSocket.this, Type.PONG));
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            publish(Type.ERROR, WebSocketData.error(JavaWebSocket.this, error));
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            publish(Type.CLOSE, WebSocketData.close(JavaWebSocket.this, statusCode, reason));
            return Polyfill.infiniteFuture();
        }

        @Nullable
        private void handleData(WebSocket webSocket, boolean last, String newData) {
            sb.append(newData);

            if (last) {
                final UniNode node = seriLib.createUniNode(sb.toString());
                publish(Type.DATA, WebSocketData.ofNode(JavaWebSocket.this, node));
                sb = new StringBuilder();
            }

            webSocket.request(1);
        }
    }
}
