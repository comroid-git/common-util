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
import java.util.concurrent.ForkJoinPool;

public final class JavaWebSocket extends UUIDContainer implements org.comroid.restless.socket.WebSocket {
    private final ListnrCore listnrCore = new ListnrCore(ForkJoinPool.commonPool()); // todo
    private final WebSocket.Listener listener = new JListener();
    private final SerializationAdapter<?, ?, ?> seriLib;

    @Override
    public ListnrCore getListnrCore() {
        return listnrCore;
    }

    public JavaWebSocket(SerializationAdapter<?, ?, ?> seriLib) {
        this.seriLib = seriLib;
    }

    public static CompletableFuture<JavaWebSocket> create(
            SerializationAdapter<?, ?, ?> seriLib,
            HttpClient httpClient,
            URI uri,
            REST.Header.List headers
    ) {
        final WebSocket.Builder builder = httpClient.newWebSocketBuilder();

        headers.forEach(builder::header);

        final JavaWebSocket socket = new JavaWebSocket(seriLib);

        return builder.buildAsync(uri, socket.listener)
                .thenApply(nil -> socket);
    }

    private final class JListener implements WebSocket.Listener {
        private StringBuilder sb = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            getListnrCore().publish(JavaWebSocket.this, Type.OPEN, WebSocketData.empty(JavaWebSocket.this, Type.OPEN));
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            return handleData(webSocket, last, data.toString());
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            return handleData(webSocket, last, new String(data.array()));
        }

        @Override
        public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
            getListnrCore().publish(
                    JavaWebSocket.this,
                    Type.PING,
                    WebSocketData.empty(JavaWebSocket.this, Type.PING)
            );
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
            getListnrCore().publish(
                    JavaWebSocket.this,
                    Type.PONG,
                    WebSocketData.empty(JavaWebSocket.this, Type.PONG)
            );
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            getListnrCore().publish(
                    JavaWebSocket.this,
                    Type.ERROR,
                    WebSocketData.error(JavaWebSocket.this, error));
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            getListnrCore().publish(
                    JavaWebSocket.this,
                    Type.CLOSE,
                    WebSocketData.close(JavaWebSocket.this, statusCode, reason)
            );
            return Polyfill.infiniteFuture();
        }

        @Nullable
        private CompletionStage<?> handleData(WebSocket webSocket, boolean last, String newData) {
            sb.append(newData);

            if (last) {
                final UniNode node = seriLib.createUniNode(sb.toString());
                getListnrCore().publish(
                        JavaWebSocket.this,
                        Type.DATA,
                        WebSocketData.ofNode(JavaWebSocket.this, node));
                sb = new StringBuilder();
            }

            webSocket.request(1);
            return null;
        }
    }
}
