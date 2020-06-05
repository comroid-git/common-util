package org.comroid.restless.socket.event.multipart;

import org.comroid.listnr.model.EventContainer;
import org.comroid.restless.socket.WebSocket;
import org.comroid.uniform.node.UniObjectNode;

public interface WebSocketEventContainer<T extends WebSocketEventType<T, P>, P extends WebSocketEventPayload<T, P>>
        extends EventContainer<UniObjectNode, WebSocket, T, P> {
    class Basic<T extends WebSocketEventType<T, P>, P extends WebSocketEventPayload<T, P>>
            implements WebSocketEventContainer<T, P> {
        private final T type;

        @Override
        public final T getType() {
            return type;
        }

        public Basic(T type) {
            this.type = type;
        }
    }
}
