package org.comroid.restless.socket;

import java.util.ArrayList;
import java.util.function.Function;

import org.comroid.dreadpool.ThreadPool;
import org.comroid.listnr.EventHub;
import org.comroid.uniform.node.UniObjectNode;

public abstract class WebSocket<EX> {
    protected WebSocket(
            ThreadGroup threadGroup, Function<EX, UniObjectNode> exchangePreprocessor
    ) {
        this.eventHub       = new EventHub<>(ThreadPool.fixedSize(threadGroup, 4), exchangePreprocessor);
        this.eventContainer = new SocketEvent.Container(this, eventHub);
    }

    public static final class Header {
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
        private final String name;
        private final String value;
    }
    private final EventHub<EX, UniObjectNode> eventHub;
    private final SocketEvent.Container       eventContainer;
}
