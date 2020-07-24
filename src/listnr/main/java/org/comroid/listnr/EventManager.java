package org.comroid.listnr;

import org.comroid.listnr.impl.PipeAccessor;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.span.Span;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EventManager<I extends EventPayload, T extends EventType<? super I, ? extends P>, P extends EventPayload> {
    UUID getUUID();

    Span<EventManager<?, ?, I>> getParents();

    Span<EventManager<P, ?, ?>> getChildren();

    Span<? extends T> getEventTypes();

    <XP extends P> Pipe<?, XP> eventPipe(EventType<I, XP> type);

    CompletableFuture<?> publish(I payload);

    @Internal
    <XP extends P> PipeAccessor<I, XP> getPipeAccessor(EventType<I, XP> eventType);
}
