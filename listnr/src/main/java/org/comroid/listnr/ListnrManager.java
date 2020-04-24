package org.comroid.listnr;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ListnrManager<I, O, E extends EventType<P, I, O>, P extends Event<P>> implements AutoCloseable {
    ListnrManager(EventHub<I, O> hub, EventAcceptor<E, P> underlying) {
        this.hub        = hub;
        this.underlying = underlying;
    }

    public ScheduledFuture<?> detachIn(long time, TimeUnit unit) {
        return hub.getExecutorService()
                .schedule(this::close, time, unit);
    }

    @Override
    public void close() {
        detachNow();
    }

    public boolean detachNow() {
        return hub.unregisterAcceptor(underlying);
    }
    private final EventHub<I, O>      hub;
    private final EventAcceptor<E, P> underlying;
}
