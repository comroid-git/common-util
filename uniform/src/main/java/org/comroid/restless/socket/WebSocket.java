package org.comroid.restless.socket;

import org.comroid.listnr.EventHub;
import org.comroid.restless.socket.internal.WebSocketEventPayload;

import java.util.Collection;

public interface WebSocket {
    EventHub<? extends WebSocketEventPayload>

    SocketListener getSocketListener();

    Collection<? extends SocketEvent> getEventTypes();

    <E extends SocketEvent> void listenTo(E eventType, SocketEvent.Handler<E> handler);
}
