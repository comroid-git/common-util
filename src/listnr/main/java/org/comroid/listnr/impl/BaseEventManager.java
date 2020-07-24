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

public class BaseEventManager<I extends EventPayload, T extends EventType<? super I, ? extends P>, P extends EventPayload>
        extends AbstractEventManager<I, T, P> {
    private final Map<String, PumpAccessor<? extends P>> accessors = TrieMap.ofString();

    public BaseEventManager() {
        this(ForkJoinPool.commonPool());
    }

    public BaseEventManager(Executor executor) {
        super(executor);
    }

    @Override
    protected <XP extends P> BaseEventManager<I, T, P>.PumpAccessor<XP> getPipeAccessor(EventType<I, XP> type) {
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

        private PumpAccessor(@NotNull EventType<I, XP> eventType) {
            this.basePump = Pump.create(executor);
            this.accessorPipe = basePump
                    .filter(eventType::triggeredBy)
                    .map(eventType::createPayload);
        }

        private <X> Pipe<?, X> castAccessor() {
            return Polyfill.uncheckedCast(accessorPipe);
        }
    }
}
