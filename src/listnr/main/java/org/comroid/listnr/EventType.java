package org.comroid.listnr;

import java.util.function.Function;
import java.util.function.Predicate;

public interface EventType<I, P extends EventPayload> extends Predicate<I>, Function<I, P>, Named {
    @Override
    boolean test(I i);

    @Override
    P apply(I i);
}
