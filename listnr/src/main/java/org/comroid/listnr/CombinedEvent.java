package org.comroid.listnr;

import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.Set;

public interface CombinedEvent<S extends EventSender<S, ? extends CombinedEvent<S>>> {
    S getSender();

    Set<EventType<?, S, ? extends CombinedEvent<S>>> getTypes();

    @Internal
    int getMask();
}
