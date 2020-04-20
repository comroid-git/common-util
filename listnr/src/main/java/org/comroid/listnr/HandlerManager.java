package org.comroid.listnr;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface HandlerManager<T extends EventSender<T, ? extends E>, E extends CombinedEvent<?>> {
    T attachedTo();

    default boolean detachNow() {
        return attachedTo().detachManager(this);
    }

    default ScheduledFuture<?> detachIn(long time, TimeUnit unit) {
        return attachedTo().getThreadPool()
                .schedule(this::detachNow, time, unit);
    }
}
