package org.comroid.mutatio.pipe;

import org.comroid.mutatio.pump.Pump;

import java.util.Collection;
import java.util.concurrent.Executor;

import static org.jetbrains.annotations.ApiStatus.OverrideOnly;

public interface Pipeable<T> {
    Pipe<?, T> pipe();

    default Pump<?, T> pump() {
        return pump(Runnable::run);
    }

    Pump<?, T> pump(Executor executor);

    interface From<T> extends Pipeable<T> {
        @Override
        default Pipe<?, T> pipe() {
            return Pipe.of(fetchPipeContent());
        }

        @OverrideOnly
        Collection<T> fetchPipeContent();
    }
}
