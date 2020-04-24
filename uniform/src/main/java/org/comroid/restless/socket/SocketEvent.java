package org.comroid.restless.socket;

import org.comroid.listnr.Event;
import org.comroid.listnr.EventHub;
import org.comroid.listnr.EventType;
import org.comroid.uniform.node.UniObjectNode;

public final class SocketEvent {
    public static final class Container<EX> {
        private final   WebSocket<EX>                         webSocket;
        private final   EventHub<EX, UniObjectNode>           eventHub;

        Container(WebSocket<EX> webSocket, EventHub<EX, UniObjectNode> eventHub) {
            this.webSocket = webSocket;
            this.eventHub  = eventHub;
        }

        public final class Generic extends Abstract<Generic> {
            private Generic(WebSocket<EX> socket) {
                super(socket);
            }
        }

        private abstract class Abstract<S extends Abstract<S>> extends Event.Support.Abstract<S> implements Event<S> {
            private final WebSocket<EX> socket;

            protected Abstract(EventType<? extends S, ?, ?> subtypes, WebSocket<EX> socket) {
                super(subtypes);

                this.socket = socket;
            }

            private Abstract(WebSocket<EX> socket) {
                this.socket = socket;
            }

            public WebSocket<EX> getSocket() {
                return socket;
            }
        }
    }
}
