package org.comroid.uniform.adapter.http.jdk;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.comroid.common.func.ParamFactory;
import org.comroid.dreadpool.ThreadPool;
import org.comroid.listnr.EventAcceptor;
import org.comroid.listnr.EventHub;
import org.comroid.listnr.EventType;
import org.comroid.restless.socket.SocketMessage;
import org.comroid.restless.socket.WebSocket;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniNode;

public class JavaWebSocket implements WebSocket {
    final         CompletableFuture<java.net.http.WebSocket> socket       = new CompletableFuture<>();
    final         java.net.http.WebSocket.Listener javaListener = new Listener();
    private final WebSocketHandler                 webSocketHandler;
    private final EventHub<UniNode>                eventHub;
    private final EventType<SocketMessage, UniNode>          socketEventType;
    private final SerializationAdapter<?, ?, ?>              seriLib;

    public JavaWebSocket(
            ThreadGroup threadGroup, SerializationAdapter<?, ?, ?> seriLib
    ) {
        this.eventHub         = new EventHub<>(ThreadPool.fixedSize(threadGroup, 4));
        this.seriLib          = seriLib;
        this.socketEventType  = eventHub.createEventType(SocketMessage.class,
                new ParamFactory.Abstract<>(data -> new SocketMessage.Basic(this, data)), eventTester
        );
        this.webSocketHandler = new WebSocketHandler();
    }

    @Override
    public EventHub<UniNode> getEventHub() {
        return eventHub;
    }

    @Override
    public EventType<SocketMessage, UniNode> getBoundEventType() {
        return socketEventType;
    }

    @Override
    public Collection<? extends SocketMessage> getEventTypes() {
        return null;
    }

    @Override
    public SerializationAdapter<?, ?, ?> getSerializationLibrary() {
        return seriLib;
    }

    private class WebSocketHandler extends EventAcceptor.Support.Abstract<EventType<SocketMessage, ?>, SocketMessage> {
        @Override
        public <T extends SocketMessage> void acceptEvent(T eventPayload) {
        }
    }

    private class Listener implements java.net.http.WebSocket.Listener {
        @Override
        public void onOpen(java.net.http.WebSocket webSocket) {

        }

        @Override
        public CompletionStage<?> onText(
                java.net.http.WebSocket webSocket, CharSequence data, boolean last
        ) {
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(
                java.net.http.WebSocket webSocket, ByteBuffer data, boolean last
        ) {
            return null;
        }

        @Override
        public CompletionStage<?> onPing(java.net.http.WebSocket webSocket, ByteBuffer message) {
            return null;
        }

        @Override
        public CompletionStage<?> onPong(java.net.http.WebSocket webSocket, ByteBuffer message) {
            return null;
        }

        @Override
        public CompletionStage<?> onClose(
                java.net.http.WebSocket webSocket, int statusCode, String reason
        ) {
            return null;
        }

        @Override
        public void onError(java.net.http.WebSocket webSocket, Throwable error) {

        }
    }
}
