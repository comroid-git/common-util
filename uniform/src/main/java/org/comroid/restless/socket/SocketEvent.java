package org.comroid.restless.socket;

import org.comroid.common.func.ParamFactory;
import org.comroid.listnr.Event;
import org.comroid.listnr.EventHub;
import org.comroid.listnr.EventType;
import org.comroid.uniform.node.UniObjectNode;

public final class SocketEvent {
    public static final class Container<EX> {
        Container(WebSocket<EX> webSocket, EventHub<EX, UniObjectNode> eventHub) {
            this.webSocket = webSocket;
            this.eventHub  = eventHub;

            this.genericType = eventHub.createEventType(Generic.class,
                    new ParamFactory.Abstract<>(data -> new Generic(this.webSocket)),
                    data -> true
            );
        }

        public final class Generic extends Abstract<Generic> {
            private Generic(WebSocket<EX> socket) {
                super(socket);
            }
        }

        private abstract class Abstract<S extends Abstract<S>> extends Event.Support.Abstract<S> implements Event<S> {
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
            private final WebSocket<EX> socket;
        }
        private final WebSocket<EX> webSocket;
        private final EventHub<EX, UniObjectNode> eventHub;
        protected final EventType<Generic, EX, UniObjectNode> genericType;
    }
}
