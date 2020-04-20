package org.comroid.listnr.model;

import org.comroid.common.util.BitmaskUtil;
import org.comroid.listnr.CombinedEvent;
import org.comroid.listnr.EventSender;
import org.comroid.listnr.EventType;

public abstract class AbstractEventType<TF, S extends EventSender<S, ? extends E>, E extends CombinedEvent<?>>
        implements EventType<TF, S, E> {
    private final int flag = BitmaskUtil.nextFlag();

    @Override
    public boolean test(TF fromType) {
        return false;
    }

    @Override
    public final int getFlag() {
        return flag;
    }
}
