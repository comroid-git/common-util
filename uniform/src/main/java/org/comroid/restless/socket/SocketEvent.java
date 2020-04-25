package org.comroid.restless.socket;

import org.comroid.listnr.Event;
import org.comroid.listnr.EventHub;
import org.comroid.listnr.EventType;

public final class SocketEvent {
    public static final class Container<O> {
        private final WebSocket<O>        webSocket;
        private final EventHub<String, O> eventHub;

        Container(WebSocket<O> webSocket, EventHub<String, O> eventHub) {
            this.webSocket = webSocket;
            this.eventHub  = eventHub;
        }

        public final class Generic extends Abstract<Generic> {
            private Generic(WebSocket<O> socket) {
                super(socket);
            }
        }

        private abstract class Abstract<S extends Abstract<S>> extends Event.Support.Abstract<S> implements Event<S> {
            private final WebSocket<O> socket;

            protected Abstract(EventType<? extends S, ?, ?> subtypes, WebSocket<O> socket) {
                super(subtypes);

                this.socket = socket;
            }

            private Abstract(WebSocket<O> socket) {
                this.socket = socket;
            }

            public WebSocket<O> getSocket() {
                return socket;
            }
        }
    }
}
