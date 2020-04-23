package org.comroid.uniform.adapter.http.jdk;

import java.util.Collection;

import org.comroid.common.func.ParamFactory;
import org.comroid.dreadpool.ThreadPool;
import org.comroid.listnr.EventHub;
import org.comroid.listnr.EventType;
import org.comroid.restless.socket.SocketListener;
import org.comroid.restless.socket.SocketMessage;
import org.comroid.restless.socket.WebSocket;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniNode;

public class JavaWebSocket implements WebSocket {
    private final EventHub<UniNode>                 eventHub;
    private final EventType<SocketMessage, UniNode> acceptedType;
    private final SerializationAdapter<?, ?, ?>     seriLib;

    public JavaWebSocket(
            ThreadGroup threadGroup, SerializationAdapter<?, ?, ?> seriLib
    ) {
        this.eventHub     = new EventHub<>(ThreadPool.fixedSize(threadGroup, 4));
        this.seriLib      = seriLib;
        this.acceptedType = eventHub.createEventType(
                SocketMessage.class,
                new ParamFactory.Abstract<>(data -> new SocketMessage(this, data))
        );
    }

    @Override
    public EventHub<UniNode> getEventHub() {
        return eventHub;
    }

    @Override
    public EventType<SocketMessage, UniNode> getAcceptedType() {
        return acceptedType;
    }

    @Override
    public SocketListener getSocketListener() {
        return null;
    }

    @Override
    public Collection<? extends SocketMessage> getEventTypes() {
        return null;
    }

    @Override
    public SerializationAdapter<?, ?, ?> getSerializationLibrary() {
        return seriLib;
    }
}
