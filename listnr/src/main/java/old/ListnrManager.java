package old;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ListnrManager<I, O, E extends EventType<? super I, ? super O, ? super P>, P extends Event<? super P>> implements AutoCloseable {
    private final EventHub<I, O, E, P>      hub;
    private final EventAcceptor<E, P> underlying;

    ListnrManager(EventHub<I, O, E, P> hub, EventAcceptor<E, P> underlying) {
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
