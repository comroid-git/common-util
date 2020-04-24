package org.comroid.restless.socket;

import java.util.ArrayList;

import org.comroid.dreadpool.ThreadPool;
import org.comroid.listnr.EventHub;
import org.comroid.uniform.node.UniObjectNode;

import com.sun.net.httpserver.HttpHandler;

public abstract class WebSocket {
    public static final class Header {
        public static final class List extends ArrayList<Header> {
            public boolean add(String name, String value) {
                return super.add(new Header(name, value));
            }
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        private final String name;
        private final String value;

        public Header(String name, String value) {
            this.name  = name;
            this.value = value;
        }
    }

    private final EventHub<HttpHandler, UniObjectNode> eventHub;
    private final SocketEvent.Container                eventContainer;

    protected WebSocket(ThreadGroup threadGroup) {
        this.eventHub       = new EventHub<>(ThreadPool.fixedSize(threadGroup, 4), preprocessor);
        this.eventContainer = new SocketEvent.Container(this, eventHub);
    }
}
