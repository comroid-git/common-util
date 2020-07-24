package org.comroid.listnr.impl;

import org.comroid.listnr.EventManager;
import org.comroid.listnr.EventPayload;
import org.comroid.listnr.EventType;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public class UnderlyingEventManager<I extends EventPayload, T extends EventType<? super I, ? extends P>, P extends EventPayload>
        extends AbstractEventManager<I, T, P> {
    private final EventManager<I, T, P> underlying;

    public UnderlyingEventManager(EventManager<I, T, P> underlying) {
        this(ForkJoinPool.commonPool(), underlying);
    }

    public UnderlyingEventManager(Executor executor, EventManager<I, T, P> underlying) {
        super(executor);

        this.underlying = underlying;
    }

    @Override
    public <XP extends P> PipeAccessor<I, XP> getPipeAccessor(EventType<I, XP> eventType) {
        return underlying.getPipeAccessor(eventType);
    }
}
