package org.comroid.listnr;

import java.util.function.Predicate;

public interface EventType<TF, S extends EventSender<S, ? extends E>, E extends Event<?>> extends Predicate<TF> {
    @Override
    boolean test(TF fromType);

    E createEvent(S sender, TF data);
}
