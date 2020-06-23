package org.comroid.listnr;

import org.comroid.api.UUIDContainer;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.span.Span;

import java.util.Objects;

import static org.comroid.api.Polyfill.uncheckedCast;

public abstract class AbstractEventManager<I, T extends EventType<? super I, ? super P>, P extends EventPayload> extends UUIDContainer implements EventManager<I, T, P> {
    private final Span<EventManager<? super I, ? super T, ? super P>> parents;
    private final Reference<ListnrCore> listnr;

    public AbstractEventManager(ListnrCore listnr) {
        this.parents = Span.empty();
        this.listnr = Reference.constant(Objects.requireNonNull(listnr));
    }

    @SafeVarargs
    public AbstractEventManager(EventManager<? super I, ? super T, ? super P>... parents) {
        if (parents.length == 0)
            throw new IllegalArgumentException("Cannot define zero parents!");

        this.parents = Span.immutable(parents);
        this.listnr = this.parents.pipe()
                .map(EventManager::listnr)
                .findAny();
    }

    @Override
    public final Span<EventManager<? super I, ? super T, ? super P>> getParents() {
        return parents;
    }

    @Override
    public final ListnrCore listnr() {
        return listnr.get();
    }

    @Override
    public final <XP extends P> Pipe<?, XP> eventPipe(EventType<I, XP> type) {
        return listnr().eventPipe(type, uncheckedCast(this));
    }

    @Override
    public final <XP extends P> void publish(EventType<I, XP> type, I payload) {
        listnr().publish(type, uncheckedCast(this), payload);
    }
}
