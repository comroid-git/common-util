package org.comroid.restless.socket;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.comroid.dreadpool.ThreadPool;
import org.comroid.listnr.EventHub;

public abstract class WebSocket<O> {
    private final EventHub<String, O>      eventHub;
    private final SocketEvent.Container<O> eventContainer;

    protected WebSocket(
            ThreadGroup threadGroup, Function<String, O> exchangePreprocessor
    ) {
        this.eventHub       = new EventHub<>(ThreadPool.fixedSize(threadGroup, 4), exchangePreprocessor);
        this.eventContainer = new SocketEvent.Container<>(this, eventHub);
    }

    public final EventHub<String, O> getEventHub() {
        return eventHub;
    }

    public final SocketEvent.Container<O> getEventContainer() {
        return eventContainer;
    }

    public abstract IntFunction<String> getCloseCodeResolver();

    public abstract void setCloseCodeResolver(IntFunction<String> closeCodeResolver);

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
