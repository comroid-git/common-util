package org.comroid.restless.socket;

import org.comroid.spellbind.annotation.Partial;
import org.comroid.listnr.Event;
import org.comroid.uniform.node.UniNode;
import org.comroid.varbind.VarCarrier;
import org.comroid.varbind.VariableCarrier;

@Partial
public interface SocketMessage extends Event<SocketMessage>, VarCarrier.Underlying<Object> {
    class Basic extends Event.Support.Abstract<SocketMessage> implements SocketMessage {
        private final WebSocket          socket;
        private final VarCarrier<Object> underlyingVarCarrier;

        public Basic(
                WebSocket socket, UniNode parameter
        ) {
            this.socket               = socket;
            this.underlyingVarCarrier = new VariableCarrier<>(socket.getSerializationLibrary(),
                    parameter.asObjectNode()
            );
        }

        @Override
        public WebSocket getWebSocket() {
            return socket;
        }

        @Override
        public VarCarrier<Object> getUnderlyingVarCarrier() {
            return underlyingVarCarrier;
        }
    }

    WebSocket getWebSocket();
}
