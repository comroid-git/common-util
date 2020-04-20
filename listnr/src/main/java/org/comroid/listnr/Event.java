package org.comroid.listnr;

import java.util.Set;

public interface Event<S extends EventSender<S, ? extends Event<S>>> {
    S getSender();

    Set<EventType<?, S, ? extends Event<S>>> getTypes();
}
