package org.comroid.listnr;

import java.util.Set;

public interface Event<S extends EventSender<? extends EventSender<?, ? extends Event<S>>, ? extends Event<?>>> {
    S getSender();

    Set<EventType<?, S, ? extends Event<S>>> getTypes();
}
