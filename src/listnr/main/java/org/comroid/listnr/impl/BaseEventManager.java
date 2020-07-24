package org.comroid.listnr.impl;

import org.comroid.api.Polyfill;
import org.comroid.listnr.EventPayload;
import org.comroid.listnr.EventType;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pump.Pump;
import org.comroid.trie.TrieMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public class BaseEventManager<D, I extends EventPayload, T extends EventType<? super D, ? super I, ? extends P>, P extends EventPayload>
        extends AbstractEventManager<D, I, T, P> {
    private final Map<String, PumpAccessor<? extends P>> accessors = TrieMap.ofString();

    @SafeVarargs
    public BaseEventManager(T... eventTypes) {
        this((D) null, eventTypes);
    }

    @SafeVarargs
    public BaseEventManager(D dependent, T... eventTypes) {
        this(dependent, ForkJoinPool.commonPool(), eventTypes);
    }

    @SafeVarargs
    public BaseEventManager(Executor executor, T... eventTypes) {
        this(null, executor, eventTypes);
    }

    @SafeVarargs
    public BaseEventManager(D dependent, Executor executor, T... eventTypes) {
        super(dependent, executor, eventTypes);
    }

    @Override
    public <XP extends P> BaseEventManager<D, I, T, P>.PumpAccessor<XP> getPipeAccessor(EventType<D, I, XP> type) {
        return Polyfill.uncheckedCast(accessors.computeIfAbsent(type.getName(), key -> new PumpAccessor<>(type)));
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final class PumpAccessor<XP extends P> implements PipeAccessor<I, XP> {
        private final Pump<I, I> basePump;
        private final Pipe<?, XP> accessorPipe;

        @Override
        public Pipe<I, I> getBasePump() {
            return basePump;
        }

        @Override
        public Pipe<?, XP> getAccessorPipe() {
            return accessorPipe;
        }

        private PumpAccessor(@NotNull EventType<D, I, XP> eventType) {
            this.basePump = Pump.create(executor);
            this.accessorPipe = basePump
                    .filter(eventType::triggeredBy)
                    .map(input -> eventType.createPayload(input, getDependent()));
        }

        private <X> Pipe<?, X> castAccessor() {
            return Polyfill.uncheckedCast(accessorPipe);
        }
    }
}
