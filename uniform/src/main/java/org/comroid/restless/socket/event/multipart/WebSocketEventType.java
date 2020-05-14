package org.comroid.restless.socket.event.multipart;

import org.comroid.common.func.Invocable;
import org.comroid.listnr.model.EventType;
import org.comroid.restless.socket.WebSocket;
import org.comroid.restless.socket.model.WebSocketBound;
import org.comroid.uniform.node.UniObjectNode;

import java.util.Collections;

public interface WebSocketEventType<P extends WebSocketEventPayload<? extends WebSocketEventType<? super P>>>
        extends EventType<UniObjectNode, WebSocket, P>, WebSocketBound {
    class Basic<P extends WebSocketEventPayload<? extends WebSocketEventType<? super P>>>
            extends EventType.Basic<UniObjectNode, WebSocket, P>
            implements WebSocketEventType<P> {
        private final Invocable.TypeMap<? extends P> payloadGenerator;

        @Override
        public final WebSocket getWebSocket() {
            return getDependent();
        }

        @Override
        public final Invocable.TypeMap<? extends P> getInstanceSupplier() {
            return payloadGenerator;
        }

        public Basic(WebSocket webSocket, Class<P> payloadType) {
            super(Collections.emptyList(), payloadType, webSocket);

            this.payloadGenerator = Invocable.<P>ofMethodCall(this, "createPayload").typeMapped();
        }
    }
}
