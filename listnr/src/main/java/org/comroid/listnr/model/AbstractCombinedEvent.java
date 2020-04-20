package org.comroid.listnr.model;

import org.comroid.listnr.CombinedEvent;
import org.comroid.listnr.EventSender;

public abstract class AbstractCombinedEvent<S extends EventSender<? extends EventSender<?, ? extends CombinedEvent<S>>, ?
        extends CombinedEvent<S>>>
        implements CombinedEvent<S> {
    private final S sender;

    public AbstractCombinedEvent(S sender) {
        this.sender = sender;
    }

    @Override
    public S getSender() {
        return sender;
    }
}
