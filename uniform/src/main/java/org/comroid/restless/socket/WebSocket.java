package org.comroid.restless.socket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.comroid.common.iter.Span;
import org.comroid.dreadpool.ThreadPool;
import org.comroid.listnr.EventHub;
import org.comroid.uniform.node.UniNode;

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

    public final CompletableFuture<Void> sendData(UniNode data) {
        final String string = data.toString();

        if (string.length() < 2048) {
            return sendString(string, true);
        } else {
            final Collection<String> substrings = new Span<>((string.length() / 2048) + 1, true);

            for (int i = 0; i < substrings.size(); i++) {
                substrings.add(string.substring((i - 1) * 2048, i * 2048));
            }
        }
    }

    protected abstract CompletableFuture<Void> sendString(String data, boolean last);

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
            public List add(String name, String value) {
                super.add(new Header(name, value));
                return this;
            }
        }
    }
}
