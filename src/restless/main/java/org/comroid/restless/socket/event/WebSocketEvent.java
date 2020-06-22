package org.comroid.restless.socket.event;

import org.comroid.listnr.EventType;
import org.comroid.restless.socket.WebSocketData;

import java.util.function.Function;

public interface WebSocketEvent extends EventType<WebSocketData, WebSocketPayload> {
    enum Type implements WebSocketEvent {
        OPEN(WebSocketPayload.Open::new),
        DATA(WebSocketPayload.Data::new),
        PING(WebSocketPayload.Ping::new),
        PONG(WebSocketPayload.Pong::new),
        ERROR(WebSocketPayload.Error::new),
        CLOSE(WebSocketPayload.Close::new);

        private final Function<WebSocketData, WebSocketPayload> remapper;

        @Override
        public String getName() {
            return null;
        }

        Type(Function<WebSocketData, WebSocketPayload> remapper) {
            this.remapper = remapper;
        }

        @Override
        public boolean test(WebSocketData webSocketData) {
            return webSocketData.getType() == this;
        }

        @Override
        public WebSocketPayload apply(WebSocketData webSocketData) {
            return remapper.apply(webSocketData);
        }
    }
}
