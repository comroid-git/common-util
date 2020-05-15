package org.comroid.listnr.model;

import org.comroid.listnr.ListnrCore;

public interface EventContainer<IN, D, ET extends EventType<IN, D, ? super EP>, EP extends EventPayload<D, ? super ET>> {
    ET getType();

    default EventContainer<IN, D, ET, EP> registerAt(ListnrCore<IN, D, ? super ET, ? super EP> core) {
        core.register(getType());
        return this;
    }
}
