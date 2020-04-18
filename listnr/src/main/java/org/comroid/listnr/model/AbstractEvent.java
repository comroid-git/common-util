package org.comroid.listnr.model;

import org.comroid.listnr.Event;
import org.comroid.listnr.EventSender;

public class AbstractEvent<S extends EventSender<? extends EventSender<?, ? extends Event<S>>, ? extends Event<S>>>
        implements Event<S> {
    private final S sender;

    public AbstractEvent(S sender) {
        this.sender = sender;
    }

    @Override
    public S getSender() {
        return sender;
    }
}
