package org.comroid.restless.socket;

import org.comroid.uniform.node.UniNode;

public interface SocketEvent {
    boolean isEvent(UniNode data);

    @FunctionalInterface
    interface Handler<E extends SocketEvent> {
        void handleEvent(E event);
    }
}
