package org.comroid.restless.socket.event;

import org.comroid.restless.socket.WebSocket;
import org.comroid.restless.socket.event.multipart.WebSocketEventContainer;
import org.comroid.restless.socket.event.multipart.WebSocketEventPayload;
import org.comroid.restless.socket.event.multipart.WebSocketEventType;

public interface CloseEvent {
    static WebSocketEventContainer<Type, Payload> container(WebSocket webSocket) {
        return new Support.Container(webSocket);
    }

    interface Type extends WebSocketEventType<Type, Payload> {
    }

    interface Payload extends WebSocketEventPayload<Type, Payload> {
        int getStatusCode();
    }

    final class Support {
        private static final class Container extends WebSocketEventContainer.Basic<Type, Payload> {
            public Container(WebSocket webSocket) {
                super(new TypeImpl(webSocket, Payload.class));
            }
        }

        private static final class TypeImpl extends WebSocketEventType.Basic<Type, Payload> implements Type {
            public TypeImpl(WebSocket webSo, Class<Payload> payloadType) {
                super(webSo, payloadType);
            }

            public Payload createPayload(int statusCode) {
                return new PayloadImpl(statusCode, this);
            }
        }

        private static final class PayloadImpl extends WebSocketEventPayload.Base<Type, Payload> implements Payload {
            private final int statusCode;

            @Override
            public int getStatusCode() {
                return statusCode;
            }

            public PayloadImpl(int statusCode, Type masterEventType) {
                super(masterEventType);
                this.statusCode = statusCode;
            }
        }
    }
}
