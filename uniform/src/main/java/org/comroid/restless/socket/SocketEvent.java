package org.comroid.restless.socket;

import org.comroid.listnr.Event;
import org.comroid.listnr.EventHub;
import org.comroid.listnr.EventType;
import org.comroid.uniform.node.UniObjectNode;

public final class SocketEvent {
    public static final class Container {
        private final WebSocket webSocket;
        private final EventHub<UniObjectNode> eventHub;

        private final EventType<Generic, UniObjectNode> genericType;

        Container(WebSocket webSocket, EventHub<UniObjectNode> eventHub) {
            this.webSocket = webSocket;
            this.eventHub = eventHub;

            this.genericType = eventHub.createEventType(Generic.class, )
        }
    }

    private static abstract class Abstract<S extends Abstract<S>> extends Event.Support.Abstract<S> implements Event<S> {
        public WebSocket getSocket() {
            return socket;
        }

        private final WebSocket socket;

        protected Abstract(EventType<?, ?> subtypes, WebSocket socket) {
            super(subtypes);

            this.socket = socket;
        }

        private Abstract(WebSocket socket) {
            this.socket = socket;
        }
    }

    public static final class Generic extends Abstract<Generic> {
        private Generic(WebSocket socket) {
            super(socket);
        }
    }
}
