package org.comroid.listnr;

import org.comroid.mutatio.pipe.Pipe;

import java.util.UUID;

public interface EventManager<I, T extends EventType<? super I, ? super P>, P extends EventPayload> {
    UUID getUUID();

    ListnrCore getListnrCore();

    default <XP extends P, XT extends EventType<? super I, XP>> Pipe<?, XP> eventPipe(XT type) {
        return getListnrCore().<I, EventManager<I, XT, XP>, XT, XP>eventPipe(type, this);
    }

    default <XP extends P, XT extends T> void publish(XT type, I payload) {
        getListnrCore().publish(type, this, payload);
    }
}
