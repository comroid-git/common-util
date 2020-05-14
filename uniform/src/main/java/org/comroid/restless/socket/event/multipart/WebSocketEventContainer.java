package org.comroid.restless.socket.event.multipart;

import org.comroid.listnr.model.EventContainer;
import org.comroid.listnr.model.EventType;
import org.comroid.restless.socket.WebSocket;
import org.comroid.uniform.node.UniObjectNode;

public interface WebSocketEventContainer<T extends WebSocketEventType<P>, P extends WebSocketEventPayload<T>>
        extends EventContainer<UniObjectNode, WebSocket, T, P> {
    class Basic<T extends WebSocketEventType<P>, P extends WebSocketEventPayload<T>>
            implements WebSocketEventContainer<T, P> {
        private final EventType<UniObjectNode, WebSocket, ? super P> type;

        @Override
        public final EventType<UniObjectNode, WebSocket, ? super P> getType() {
            return type;
        }

        public Basic(EventType<UniObjectNode, WebSocket, ? super P> type) {
            this.type = type;
        }
    }
}
