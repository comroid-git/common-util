package org.comroid.listnr;

import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.span.Span;

public interface EventManager<I, T extends EventType<? super I, ? super P>, P extends EventPayload> {
    Span<EventManager<? super I, ? super T, ? super P>> getParents();

    ListnrCore listnr();

    <XP extends P> Pipe<?, XP> eventPipe(EventType<I, XP> type);

    <XP extends P> void publish(EventType<I, XP> type, I payload);
}
