package org.comroid.restless.socket;

import java.util.Collection;

public interface WebSocket {
    SocketListener getListener();

    Collection<? extends SocketEvent> getEventTypes();

    <E extends SocketEvent> void listenTo(E eventType, SocketEvent.Handler<E> handler);
}
