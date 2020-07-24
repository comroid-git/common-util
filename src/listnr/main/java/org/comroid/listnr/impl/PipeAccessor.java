package org.comroid.listnr.impl;

import org.comroid.listnr.EventPayload;
import org.comroid.listnr.EventType;
import org.comroid.mutatio.pipe.Pipe;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface PipeAccessor<I extends EventPayload, P extends EventPayload> {
    EventType<?, I, P> getEventType();

    Pipe<I, I> getBasePump();

    Pipe<?, P> getAccessorPipe();
}
