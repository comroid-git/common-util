package org.comroid.restless.socket.event.multipart;

import org.comroid.listnr.model.EventPayload;
import org.comroid.restless.socket.WebSocket;

public interface WebSocketEventPayload<
        T extends WebSocketEventType<T, P>,
        P extends WebSocketEventPayload<T, P>>
        extends EventPayload<WebSocket, T, P> {
    class Base<
            T extends WebSocketEventType<T, P>,
            P extends WebSocketEventPayload<T, P>>
            extends EventPayload.Basic<WebSocket, T, P> {
        public Base(T masterEventType) {
            super(masterEventType);
        }
    }
}
