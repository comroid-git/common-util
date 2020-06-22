package org.comroid.listnr;

import org.comroid.mutatio.pipe.Pipe;

import java.util.UUID;

import static org.comroid.api.Polyfill.uncheckedCast;

public interface EventManager<I, T extends EventType<? super I, ? super P>, P extends EventPayload> {
    UUID getUUID();

    ListnrCore listnr();

    default <XP extends P> Pipe<?, XP> eventPipe(EventType<I, XP> type) {
        return listnr().eventPipe(type, uncheckedCast(this));
    }

    default <XP extends P> void publish(EventType<I, XP> type, I payload) {
        listnr().publish(type, uncheckedCast(this), payload);
    }
}
