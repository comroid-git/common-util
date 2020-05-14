package org.comroid.restless.socket.event;

import org.comroid.restless.socket.WebSocket;
import org.comroid.restless.socket.event.multipart.WebSocketEventContainer;
import org.comroid.restless.socket.event.multipart.WebSocketEventPayload;
import org.comroid.restless.socket.event.multipart.WebSocketEventType;

public interface PingEvent {
    static WebSocketEventContainer<Type, Payload> container(WebSocket webSocket) {
        return new Support.Container(webSocket);
    }

    interface Type extends WebSocketEventType<Payload> {
    }

    interface Payload extends WebSocketEventPayload<Type> {
    }

    final class Support {
        private static final class Container extends WebSocketEventContainer.Basic<Type, Payload> {
            public Container(WebSocket webSocket) {
                super(new TypeImpl(webSocket, Payload.class));
            }
        }

        private static final class TypeImpl extends WebSocketEventType.Basic<Payload> implements Type {
            public TypeImpl(WebSocket webSo, Class<Payload> payloadType) {
                super(webSo, payloadType);
            }

            public Payload createPayload() {
                return new PayloadImpl(this);
            }
        }

        private static final class PayloadImpl extends WebSocketEventPayload.Base<Type> implements Payload {
            public PayloadImpl(Type masterEventType) {
                super(masterEventType);
            }
        }
    }
}
