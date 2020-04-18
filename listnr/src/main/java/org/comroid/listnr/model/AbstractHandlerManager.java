package org.comroid.listnr.model;

import org.comroid.listnr.Event;
import org.comroid.listnr.EventSender;
import org.comroid.listnr.HandlerManager;

public abstract class AbstractHandlerManager<T extends EventSender<T, ? extends E>, E extends Event<T>>
        implements HandlerManager<T, E> {
    private final T sender;

    public AbstractHandlerManager(T sender) {
        this.sender = sender;
    }

    @Override
    public T attachedTo() {
        return sender;
    }
}
