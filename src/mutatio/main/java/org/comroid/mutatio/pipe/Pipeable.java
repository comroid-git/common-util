package org.comroid.mutatio.pipe;

import org.comroid.api.ExecutorBound;
import org.comroid.util.FunctionUtil;
import org.comroid.mutatio.pump.Pump;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static org.jetbrains.annotations.ApiStatus.OverrideOnly;

public interface Pipeable<T> {
    Pipe<?, T> pipe();

    default Pump<?, T> pump() {
        return pump(Runnable::run);
    }

    default Pump<?, T> pump(Executor executor) {
        Executor exe = Stream.<Callable<Executor>>of(() -> executor, ((ExecutorBound) this)::getExecutor, () -> Runnable::run)
                .map(FunctionUtil::executeRethrow)
                .findAny()
                .orElseThrow(AssertionError::new);
        return pipe().pump(exe);
    }

    interface From<T> extends Pipeable<T> {
        @Override
        default Pipe<?, T> pipe() {
            return Pipe.of(fetchPipeContent());
        }

        @OverrideOnly
        Collection<T> fetchPipeContent();
    }
}
