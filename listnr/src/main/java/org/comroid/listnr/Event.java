package org.comroid.listnr;

public interface Event<S extends EventSender<? extends EventSender<?, ? extends Event<S>>, ? extends Event<S>>> {
    S getSender();
}
