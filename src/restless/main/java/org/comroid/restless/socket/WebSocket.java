package org.comroid.restless.socket;

import org.comroid.listnr.AbstractEventManager;
import org.comroid.listnr.EventManager;
import org.comroid.listnr.EventType;
import org.comroid.listnr.ListnrCore;
import org.comroid.restless.socket.event.WebSocketEvent;
import org.comroid.restless.socket.event.WebSocketPayload;
import org.comroid.uniform.node.UniNode;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public abstract class WebSocket extends AbstractEventManager<WebSocketData, WebSocketEvent<WebSocketPayload>, WebSocketPayload> {
    public static final int MAX_MESSAGE_LENGTH = 2000;

    public WebSocket(ListnrCore listnr) {
        super(listnr);
    }

    @SafeVarargs
    public WebSocket(EventManager<? super WebSocketData, ? super WebSocketEvent<WebSocketPayload>, ? super WebSocketPayload>... parents) {
        super(parents);
    }

    @Override
    public Collection<? extends EventType<? extends WebSocketData, ? extends WebSocketPayload>> getEventTypes() {
        return WebSocketEvent.VALUES;
    }

    public abstract CompletableFuture<?> sendData(UniNode data);

    public abstract CompletableFuture<?> sendClose(int statusCode, String reason);

    public abstract CompletableFuture<Long> evaluatePing();
}
