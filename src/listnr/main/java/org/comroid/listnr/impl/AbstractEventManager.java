package org.comroid.listnr.impl;

import jdk.internal.net.http.frame.PushPromiseFrame;
import org.comroid.api.Polyfill;
import org.comroid.api.UUIDContainer;
import org.comroid.listnr.EventManager;
import org.comroid.listnr.EventPayload;
import org.comroid.listnr.EventType;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pump.Pump;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.span.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public abstract class AbstractEventManager<I extends EventPayload, T extends EventType<? super I, ? extends P>, P extends EventPayload>
        extends UUIDContainer
        implements EventManager<I, T, P> {
    private final Span<EventManager<?, ?, I>> parents = new Span<>();
    private final Span<EventManager<P, ?, ?>> children = new Span<>();
    private final Span<? extends T> registeredTypes = new Span<>();
    protected final Executor executor;

    @Override
    public final Span<EventManager<?, ?, I>> getParents() {
        return parents;
    }

    @Override
    public final Span<EventManager<P, ?, ?>> getChildren() {
        return children;
    }

    @Override
    public final Span<? extends T> getEventTypes() {
        return registeredTypes;
    }

    @Override
    public final <XP extends P> Pipe<?, XP> eventPipe(EventType<I, XP> type) {
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

    protected abstract <XP extends P> PipeAccessor<I, XP> getPipeAccessor(EventType<I, XP> eventType);

    protected AbstractEventManager(Executor executor) {
        this.executor = executor;
    }
}
