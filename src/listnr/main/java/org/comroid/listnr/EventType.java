package org.comroid.listnr;

import org.comroid.common.ref.Named;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.ref.Reference;

public interface EventType<D, I extends EventPayload, P extends EventPayload> extends Named {
    Processor<EventType<?, ?, I>> getCommonCause();

    boolean triggeredBy(I oldPayload);

    P createPayload(I oldPayload, D dependent);

    default Reference<P> advance(Reference<I> inputReference) {
        return advance(inputReference, null);
    }

    default Reference<P> advance(Reference<I> inputReference, D dependent) {
        return inputReference.process()
                .filter(this::triggeredBy)
                .map(input -> createPayload(input, dependent));
    }
}
