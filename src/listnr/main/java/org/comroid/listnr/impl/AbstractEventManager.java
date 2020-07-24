package org.comroid.listnr.impl;

import org.comroid.api.Polyfill;
import org.comroid.api.UUIDContainer;
import org.comroid.listnr.EventManager;
import org.comroid.listnr.EventPayload;
import org.comroid.listnr.EventType;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pump.Pump;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.span.Span;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public abstract class AbstractEventManager<D, I extends EventPayload, T extends EventType<? super D, ? super I, ? extends P>, P extends EventPayload>
        extends UUIDContainer
        implements EventManager<D, I, T, P> {
    protected final Executor executor;
    private final Span<EventManager<?, ?, ?, I>> parents = new Span<>();
    private final Span<EventManager<?, P, ?, ?>> children = new Span<>();
    private final Span<? extends T> registeredTypes;
    private final @Nullable D dependent;

    @Override
    public final Span<EventManager<?, ?, ?, I>> getParents() {
        return parents;
    }

    @Override
    public final Span<EventManager<?, P, ?, ?>> getChildren() {
        return children;
    }

    @Override
    public final Span<? extends T> getEventTypes() {
        return registeredTypes;
    }

    @Nullable
    @Override
    public final D getDependent() {
        return dependent;
    }

    @SafeVarargs
    protected AbstractEventManager(@Nullable D dependent, Executor executor, T... eventTypes) {
        this.dependent = dependent;
        this.executor = executor;
        this.registeredTypes = Span.immutable(eventTypes);
    }

    @Override
    public final <XP extends P> Pipe<?, XP> eventPipe(EventType<D, I, XP> type) {
        return getPipeAccessor(type).getAccessorPipe();
    }

    @Override
    public final CompletableFuture<?> publish(I oldPayload) {
        // get notified by parent
        final List<Supplier<?>> executionTasks = new ArrayList<>();
        final Reference<I> opRef = Reference.constant(oldPayload);

        // prepare event propagation
        return CompletableFuture.allOf(
                getEventTypes().stream()
                        .filter(eventType -> eventType.triggeredBy(oldPayload))
                        .map(eventType -> getPipeAccessor(Polyfill.uncheckedCast(eventType)).getBasePump())
                        .filter(Pump.class::isInstance)
                        .<Supplier<?>>map(pump -> () -> {
                            pump.accept(opRef);
                            return null;
                        })
                        .map(task -> CompletableFuture.supplyAsync(task, executor))
                        .toArray(CompletableFuture[]::new));
    }
}
