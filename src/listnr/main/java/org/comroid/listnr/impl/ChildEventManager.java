package org.comroid.listnr.impl;

import org.comroid.api.Polyfill;
import org.comroid.common.exception.AssertionException;
import org.comroid.listnr.EventManager;
import org.comroid.listnr.EventPayload;
import org.comroid.listnr.EventType;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pump.Pump;
import org.comroid.trie.TrieMap;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.comroid.api.Polyfill.uncheckedCast;

public class ChildEventManager<I extends EventPayload, T extends EventType<? super I, ? extends P>, P extends EventPayload>
        extends AbstractEventManager<I, T, P> {
    private final Map<String, PumpAccessor<? extends P>> accessors = TrieMap.ofString();

    @SafeVarargs
    protected ChildEventManager(Executor executor, EventManager<?, ?, I>... parents) {
        super(executor);

        AssertionException.expect(0, parents.length, (x, y) -> x > y, "no parents defined");

        Arrays.stream(parents)
                .peek(parent -> parent.getChildren().add(this))
                .forEach(parent -> getParents().add(parent));
    }

    @Override
    protected <XP extends P> ChildEventManager<I, T, P>.PumpAccessor<XP> getPipeAccessor(EventType<I, XP> type) {
        final String key = type.getName();

        if (accessors.containsKey(key))
            return uncheckedCast(accessors.get(key));
        final Pipe<I, I> source = type.getCommonCause()
                .map(cause -> getParents().stream()
                        .<Pipe<?, I>>map(parent -> parent.eventPipe(Polyfill.uncheckedCast(cause)))
                        .filter(Pump.class::isInstance)
                        .map(Polyfill::<Pump<I, I>>uncheckedCast)
                        .collect(Pipe.resultingPipeCollector(executor))
                )
                .map(Polyfill::<Pump<I, I>>uncheckedCast)
                .orElseGet(() -> Pump.create(executor));
        return Polyfill.uncheckedCast(accessors.computeIfAbsent(key, k -> new PumpAccessor<>(source, type)));
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final class PumpAccessor<XP extends P> implements PipeAccessor<I, XP> {
        private final Pipe<I, I> basePipe;
        private final Pipe<?, XP> accessorPipe;

        @Override
        public Pipe<I, I> getBasePump() {
            return basePipe;
        }

        @Override
        public Pipe<?, XP> getAccessorPipe() {
            return accessorPipe;
        }

        private PumpAccessor(Pipe<I, I> pipe, @NotNull EventType<I, XP> eventType) {
            this.basePipe = pipe;
            this.accessorPipe = basePipe
                    .filter(eventType::triggeredBy)
                    .map(eventType::createPayload);
        }

        private <X> Pipe<?, X> castAccessor() {
            return uncheckedCast(accessorPipe);
        }
    }
}
