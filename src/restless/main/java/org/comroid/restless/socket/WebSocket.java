package org.comroid.restless.socket;

import org.comroid.listnr.AbstractEventManager;
import org.comroid.restless.socket.event.WebSocketEvent;
import org.comroid.restless.socket.event.WebSocketPayload;
import org.comroid.uniform.node.UniNode;

import java.util.concurrent.CompletableFuture;

public interface WebSocket extends AbstractEventManager<WebSocketData, WebSocketEvent<WebSocketPayload>, WebSocketPayload> {
    int MAX_MESSAGE_LENGTH = 2000;

    CompletableFuture<?> sendData(UniNode data);

    CompletableFuture<?> sendClose(int statusCode, String reason);

    CompletableFuture<Long> evaluatePing();
}
