package org.comroid.listnr.impl;

import org.comroid.listnr.EventManager;
import org.comroid.listnr.EventPayload;
import org.comroid.listnr.EventType;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public class UnderlyingEventManager<D, I extends EventPayload, T extends EventType<? super I, ? extends P>, P extends EventPayload>
        extends AbstractEventManager<D, I, T, P> {
    private final EventManager<D, I, T, P> underlying;

    public UnderlyingEventManager(EventManager<D, I, T, P> underlying) {
        this((D) null, underlying);
    }

    public UnderlyingEventManager(D dependent, EventManager<D, I, T, P> underlying) {
        this(dependent, ForkJoinPool.commonPool(), underlying);
    }

    public UnderlyingEventManager(Executor executor, EventManager<D, I, T, P> underlying) {
        this(null, executor, underlying);
    }

    public UnderlyingEventManager(D dependent, Executor executor, EventManager<D, I, T, P> underlying) {
        super(dependent, executor);

        this.underlying = underlying;
    }

    @Override
    public <XP extends P> PipeAccessor<I, XP> getPipeAccessor(EventType<I, XP> eventType) {
        return underlying.getPipeAccessor(eventType);
    }
}
