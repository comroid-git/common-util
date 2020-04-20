package org.comroid.listnr;

import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.function.Predicate;

public interface EventType<TF, S extends EventSender<S, ? extends E>, E extends CombinedEvent<?>> extends Predicate<TF> {
    @Override
    boolean test(TF fromType);

    @Internal
    int getFlag();
}
