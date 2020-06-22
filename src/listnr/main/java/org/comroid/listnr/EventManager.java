package org.comroid.listnr;

import org.comroid.mutatio.pipe.Pipe;

import java.util.UUID;

public interface EventManager<T extends EventType<?, ? super P>, P extends EventPayload> {
    UUID getUUID();

    ListnrCore getListnrCore();

    default <E extends T, D extends P> Pipe<?, D> eventPipe(E type) {
        return getListnrCore().eventPipe(this, type);
    }

    default <E extends T, D extends P> void publish(E type, P payload) {
        getListnrCore().publish(this, type, payload);
    }
}
