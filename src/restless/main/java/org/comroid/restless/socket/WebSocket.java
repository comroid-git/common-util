package org.comroid.restless.socket;

import org.comroid.listnr.impl.BaseEventManager;
import org.comroid.restless.socket.event.WebSocketEvent;
import org.comroid.restless.socket.event.WebSocketPayload;
import org.comroid.uniform.node.UniNode;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.comroid.restless.socket.event.WebSocketEvent.*;

public abstract class WebSocket extends BaseEventManager<WebSocketData, WebSocketEvent<? extends WebSocketPayload>, WebSocketPayload> {
    public static final int MAX_MESSAGE_LENGTH = 2000;

    public WebSocket(Executor executor) {
        super(executor, OPEN, DATA, PING, PONG, ERROR, CLOSE);
    }

    public abstract CompletableFuture<?> sendData(UniNode data);

    public abstract CompletableFuture<?> sendClose(int statusCode, String reason);

    public abstract CompletableFuture<Long> evaluatePing();
}
