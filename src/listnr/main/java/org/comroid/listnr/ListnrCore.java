package org.comroid.listnr;

import org.comroid.api.Polyfill;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pipe.StageAdapter;
import org.comroid.mutatio.pump.BasicPump;
import org.comroid.mutatio.pump.Pump;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.trie.TrieMap;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ListnrCore {
    private final Map<String, PipeContainer<?, ?>> pipes = TrieMap.ofString();
    private final Executor executor;

    public ListnrCore(Executor executor) {
        this.executor = executor;
    }

    public <I,
            M extends EventManager<I, ? super P, ? super T>,
            T extends EventType<I, P>,
            P extends EventPayload>
    Pipe<?, P> eventPipe(T eventType, M target) {
        return computePipe(eventType, target).pipe();
    }

    public <I,
            M extends EventManager<I, ? super P, ? super T>,
            T extends EventType<I, P>,
            P extends EventPayload>
    void publish(T eventType, M target, I payloadInput) {
        computePipe(eventType, target)
                .consumer()
                .accept(payloadInput);
    }

    private <I,
            M extends EventManager<I, ? super P, ? super T>,
            T extends EventType<I, P>,
            P extends EventPayload>
    PipeContainer<I, P> computePipe(T eventType, M target) {
        return Polyfill.uncheckedCast(pipes.computeIfAbsent(
                target.getUUID() + eventType.getName(),
                key -> new PipeContainer<>(eventType.andThen(Polyfill::<P>uncheckedCast))
        ));
    }

    private class PipeContainer<I, P extends EventPayload> {
        private final Pump<I, P> pump;
        private final Pipe<?, P> pipe;

        private PipeContainer(Function<I, P> initializer) {
            // create base stage
            this.pump = new BasicPump<>(executor, ReferenceIndex.create(), StageAdapter.map(initializer));
            // create substage in order to deny base accesses from outside
            this.pipe = pump.pipe();
        }

        private Pipe<?, P> pipe() {
            return pipe;
        }

        private Consumer<I> consumer() {
            return pump;
        }
    }
}
