package org.comroid.listnr;

import org.comroid.common.ref.Named;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.ref.Reference;

import java.util.function.Function;
import java.util.function.Predicate;

public interface EventType<I extends EventPayload, P extends EventPayload> extends Named {
    Processor<EventType<?, I>> getCommonCause();

    boolean triggeredBy(I oldPayload);

    P createPayload(I oldPayload);

    default Reference<P> advance(Reference<I> inputReference) {
        return inputReference.process()
                .filter(this::triggeredBy)
                .map(this::createPayload);
    }
}
