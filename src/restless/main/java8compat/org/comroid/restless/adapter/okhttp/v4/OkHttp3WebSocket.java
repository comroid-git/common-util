package org.comroid.restless.adapter.okhttp.v4;

import okhttp3.*;
import okio.ByteString;
import org.comroid.api.Polyfill;
import org.comroid.listnr.EventManager;
import org.comroid.listnr.ListnrCore;
import org.comroid.mutatio.ref.Reference;
import org.comroid.restless.REST;
import org.comroid.restless.socket.WebSocketData;
import org.comroid.restless.socket.event.WebSocketEvent;
import org.comroid.restless.socket.event.WebSocketPayload;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class OkHttp3WebSocket extends org.comroid.restless.socket.WebSocket {
    private final WebSocketListener listener = new OkListener();
    private final SerializationAdapter<?, ?, ?> seriLib;
    private final Reference<WebSocket> iSocketRef;

    private OkHttp3WebSocket(SerializationAdapter<?, ?, ?> seriLib, CompletableFuture<WebSocket> socketFuture, Executor executor) {
        super(new ListnrCore(executor));

        this.seriLib = seriLib;
        this.iSocketRef = Reference.later(socketFuture);
    }

    @SafeVarargs
    public OkHttp3WebSocket(
            SerializationAdapter<?, ?, ?> seriLib,
            CompletableFuture<WebSocket> iSocketRef,
            EventManager<? super WebSocketData, ? super WebSocketEvent<WebSocketPayload>, ? super WebSocketPayload>... parents
    ) {
        super(parents);
        this.seriLib = seriLib;
        this.iSocketRef = Reference.later(iSocketRef);
    }

    public static CompletableFuture<OkHttp3WebSocket> create(
            SerializationAdapter<?, ?, ?> seriLib,
            Executor executor,
            OkHttpClient httpClient,
            URI uri,
            REST.Header.List headers
    ) {
        final CompletableFuture<WebSocket> iSocketFuture = new CompletableFuture<>();
        final OkHttp3WebSocket socket = new OkHttp3WebSocket(seriLib, iSocketFuture, executor);

        final Request.Builder rqBuilder = new Request.Builder()
                .url(uri.toString());
        headers.forEach(rqBuilder::addHeader);

        final WebSocket okSocket = httpClient.newWebSocket(rqBuilder.build(), socket.listener);
        iSocketFuture.complete(okSocket);

        return CompletableFuture.completedFuture(socket);
    }

    @Override
    public CompletableFuture<Boolean> sendData(UniNode data) {
        return CompletableFuture.completedFuture(
                iSocketRef.requireNonNull("WebSocket missing")
                        .send(data.toString()));
    }

    @Override
    public CompletableFuture<Boolean> sendClose(int statusCode, String reason) {
        return CompletableFuture.completedFuture(
                iSocketRef.requireNonNull("WebSocket missing")
                        .close(statusCode, reason));
    }

    @Override
    public CompletableFuture<Long> evaluatePing() {
        return Polyfill.failedFuture(new UnsupportedOperationException());
    }

    private final class OkListener extends WebSocketListener {
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            publish(WebSocketEvent.OPEN, WebSocketData.empty(OkHttp3WebSocket.this, WebSocketEvent.OPEN));
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            publish(WebSocketEvent.DATA, WebSocketData.ofNode(OkHttp3WebSocket.this, seriLib.createUniNode(text)));
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            publish(WebSocketEvent.DATA, WebSocketData.ofNode(OkHttp3WebSocket.this, seriLib.createUniNode(bytes.utf8())));
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable throwable, @Nullable Response response) {
            publish(WebSocketEvent.ERROR, WebSocketData.error(OkHttp3WebSocket.this, throwable));
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            publish(WebSocketEvent.CLOSE, WebSocketData.close(OkHttp3WebSocket.this, code, reason));
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            publish(WebSocketEvent.CLOSE, WebSocketData.close(OkHttp3WebSocket.this, code, reason));
        }
    }
}
