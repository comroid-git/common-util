package org.comroid.restless.socket.event.multipart;

import org.comroid.listnr.model.EventPayload;
import org.comroid.restless.socket.WebSocket;

public interface WebSocketEventPayload<T extends WebSocketEventType<? extends WebSocketEventPayload<? super T>>>
        extends EventPayload<WebSocket, T> {
    class Base<T extends WebSocketEventType<? extends WebSocketEventPayload<? super T>>>
            extends EventPayload.Basic<WebSocket, T> {
        public Base(T masterEventType) {
            super(masterEventType);
        }
    }
}
