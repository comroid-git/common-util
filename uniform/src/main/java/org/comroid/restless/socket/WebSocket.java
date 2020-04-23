package org.comroid.restless.socket;

import java.util.Collection;

import org.comroid.listnr.EventHub;
import org.comroid.restless.socket.internal.WebSocketEvent;

public interface WebSocket {
    EventHub<? extends WebSocketEvent>

    SocketListener getSocketListener();

    Collection<? extends SocketEvent> getEventTypes();

    <E extends SocketEvent> void listenTo(E eventType, SocketEvent.Handler<E> handler);
}
