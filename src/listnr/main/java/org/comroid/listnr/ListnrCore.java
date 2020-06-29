package org.comroid.listnr;

import org.comroid.api.Polyfill;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pipe.StageAdapter;
import org.comroid.mutatio.pump.BasicPump;
import org.comroid.mutatio.pump.Pump;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.trie.TrieMap;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class ListnrCore {
    private final Map<String, PipeContainer<?, ?>> pipes = TrieMap.ofString();
    private final Executor executor;

    public ListnrCore(Executor executor) {
        this.executor = executor;
    }

    @Internal
    <I, P extends EventPayload> Pipe<?, P> eventPipe(
            EventType<? super I, ? extends P> eventType,
            EventManager<I, EventType<? super I, ? extends P>, ? super P> target
    ) {
        return computePipe(eventType, target).pipe();
    }

    @Internal
    <I, P extends EventPayload> void publish(
            EventType<? super I, ? extends P> eventType,
            EventManager<I, EventType<? super I, ? extends P>, ? super P> target,
            I payloadInput
    ) {
        computePipe(eventType, target).push(eventType, target, payloadInput);
    }

    @Internal
    <I, P extends EventPayload> PipeContainer<I, P> computePipe(
            EventType<? super I, ? extends P> eventType,
            EventManager<I, EventType<? super I, ? extends P>, ? super P> target
    ) {
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

        public void push(
                EventType<? super I, ? extends P> eventType,
                EventManager<I, EventType<? super I, ? extends P>, ? super P> target,
                I input
        ) {
            pump.accept(Reference.constant(input));
            target.getParents().forEach(parent -> parent.publish(Polyfill.uncheckedCast(eventType), input));
        }
    }
}
