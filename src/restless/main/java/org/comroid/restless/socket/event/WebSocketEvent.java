package org.comroid.restless.socket.event;

import org.comroid.listnr.EventType;
import org.comroid.mutatio.proc.Processor;
import org.comroid.restless.socket.WebSocketData;

import java.util.function.Function;

public interface WebSocketEvent<P extends WebSocketPayload> extends EventType<Object, WebSocketData, P> {
    WebSocketEvent<WebSocketPayload.Open> OPEN = new Base<>("websocket-open", WebSocketPayload.Open::new);
    WebSocketEvent<WebSocketPayload.Data> DATA = new Base<>("websocket-data", WebSocketPayload.Data::new);
    WebSocketEvent<WebSocketPayload.Ping> PING = new Base<>("websocket-ping", WebSocketPayload.Ping::new);
    WebSocketEvent<WebSocketPayload.Pong> PONG = new Base<>("websocket-pong", WebSocketPayload.Pong::new);
    WebSocketEvent<WebSocketPayload.Error> ERROR = new Base<>("websocket-error", WebSocketPayload.Error::new);
    WebSocketEvent<WebSocketPayload.Close> CLOSE = new Base<>("websocket-close", WebSocketPayload.Close::new);

    final class Base<P extends WebSocketPayload> implements WebSocketEvent<P> {
        private final String name;
        private final Function<WebSocketData, P> remapper;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Processor<EventType<?, ?, WebSocketData>> getCommonCause() {
            return Processor.empty();
        }

        private Base(String name, Function<WebSocketData, P> remapper) {
            this.name = name;
            this.remapper = remapper;
        }

        @Override
        public boolean triggeredBy(WebSocketData oldPayload) {
            return equals(oldPayload.getType());
        }

        @Override
        public P createPayload(WebSocketData webSocketData, Object nil) {
            return remapper.apply(webSocketData);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public boolean equals(Object obj) {
            if (obj instanceof WebSocketEvent)
                return ((WebSocketEvent) obj).getName().equals(getName());
            return false;
        }
    }
}
