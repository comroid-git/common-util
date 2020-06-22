package org.comroid.listnr;

import org.comroid.api.Polyfill;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pump.Pump;
import org.comroid.trie.TrieMap;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public final class ListnrCore {
    private final Map<String, PipeContainer<?>> pipes = TrieMap.ofString();
    private final Executor executor;

    public ListnrCore(Executor executor) {
        this.executor = executor;
    }

    public <A extends EventManager<? super T, ? super P>,
            T extends EventType<?, ? super P>,
            P extends EventPayload>
    Pipe<?, P> eventPipe(A target, T eventType) {
        return computePipe(target, eventType).pipe();
    }

    public <A extends EventManager<? super T, ? super P>,
            T extends EventType<?, ? super P>,
            P extends EventPayload>
    void publish(A target, T eventType, P payload) {
        computePipe(target, eventType)
                .consumer()
                .accept(payload);
    }

    public <I,
            A extends EventManager<? super T, ? super P>,
            T extends EventType<I, P>,
            P extends EventPayload>
    void publish(A target, T eventType, I payloadInput) {
        computePipe(target, eventType)
                .consumer()
                .accept(eventType.apply(payloadInput));
    }

    private <A extends EventManager<? super T, ? super P>,
            T extends EventType<?, ? super P>,
            P extends EventPayload>
    PipeContainer<P> computePipe(A target, T eventType) {
        return Polyfill.uncheckedCast(pipes.computeIfAbsent(
                target.getUUID() + eventType.getName(),
                PipeContainer<P>::new
        ));
    }

    private class PipeContainer<P extends EventPayload> {
        private final Pump<P, P> pump;
        private final Pipe<?, P> pipe;

        private PipeContainer(String key) {
            // create base stage
            this.pump = Pump.create(executor);
            // create substage in order to deny base accesses from outside
            this.pipe = pump.pipe();
        }

        private Pipe<?, P> pipe() {
            return pipe;
        }

        private Consumer<P> consumer() {
            return pump;
        }
    }
}
