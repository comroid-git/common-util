package org.comroid.listnr;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ListnrManager<TF, E extends EventType<P, ?>, P extends Event<P>>
        implements AutoCloseable {
    private final EventHub<TF>        hub;
    private final EventAcceptor<E, P> underlying;

    public ListnrManager(EventHub<TF> hub, EventAcceptor<E, P> underlying) {
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
}
