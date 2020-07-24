package org.comroid.listnr.impl;

import org.comroid.mutatio.pipe.Pipe;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface PipeAccessor<I, O> {
    Pipe<I, I> getBasePump();

    Pipe<?, O> getAccessorPipe();
}
