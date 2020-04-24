package org.comroid.restless.socket;

import org.comroid.dreadpool.ThreadPool;
import org.comroid.listnr.EventHub;
import org.comroid.uniform.node.UniObjectNode;

public abstract class WebSocket {
    private final EventHub<UniObjectNode> eventHub;
    private final SocketEvent.Container eventContainer;

    protected WebSocket(ThreadGroup threadGroup) {
        this.eventHub = new EventHub<>(ThreadPool.fixedSize(threadGroup, 4));
        this.eventContainer = new SocketEvent.Container(this.eventHub);
    }
}
