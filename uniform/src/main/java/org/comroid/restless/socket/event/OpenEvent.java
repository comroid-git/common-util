package org.comroid.restless.socket.event;

import org.comroid.common.func.Invocable;
import org.comroid.restless.socket.WebSocket;
import org.comroid.restless.socket.event.multipart.WebSocketEventContainer;
import org.comroid.restless.socket.event.multipart.WebSocketEventPayload;
import org.comroid.restless.socket.event.multipart.WebSocketEventType;

public interface OpenEvent {
    interface Type extends WebSocketEventType<Payload> {
    }

    interface Payload extends WebSocketEventPayload<Type> {
    }

    final class Support {
        private static final class Container extends WebSocketEventContainer.Basic<Type, Payload> {
            public Container(WebSocket webSocket) {
                super(new TypeImpl(webSocket, Payload.class, Invocable.ofMethodCall(/* todo: what to do */)));
            }
        }

        private static final class TypeImpl extends WebSocketEventType.Basic<Payload> implements Type {
            public TypeImpl(WebSocket webSo, Class<Payload> payloadType, Invocable.TypeMap<? extends Payload> payloadGenerator) {
                super(webSo, payloadType, payloadGenerator);
            }
        }

        private static final class PayloadImpl extends WebSocketEventPayload.Base<Type> implements Payload {
            public PayloadImpl(Type masterEventType) {
                super(masterEventType);
            }
        }
    }
}
