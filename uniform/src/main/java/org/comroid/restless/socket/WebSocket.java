package org.comroid.restless.socket;

import java.util.ArrayList;
import java.util.function.Function;

import org.comroid.dreadpool.ThreadPool;
import org.comroid.listnr.EventHub;
import org.comroid.uniform.node.UniObjectNode;

public abstract class WebSocket<I> {
    private final EventHub<I, UniObjectNode> eventHub;
    private final SocketEvent.Container<I>   eventContainer;

    protected WebSocket(
            ThreadGroup threadGroup, Function<I, UniObjectNode> exchangePreprocessor
    ) {
        this.eventHub       = new EventHub<>(ThreadPool.fixedSize(threadGroup, 4), exchangePreprocessor);
        this.eventContainer = new SocketEvent.Container<>(this, eventHub);
    }

    protected final EventHub<I, UniObjectNode> getEventHub() {
        return eventHub;
    }

    protected final SocketEvent.Container<I> getEventContainer() {
        return eventContainer;
    }

    public static final class Header {
        private final String name;
        private final String value;

        public Header(String name, String value) {
            this.name  = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public static final class List extends ArrayList<Header> {
            public boolean add(String name, String value) {
                return super.add(new Header(name, value));
            }
        }
    }
}
