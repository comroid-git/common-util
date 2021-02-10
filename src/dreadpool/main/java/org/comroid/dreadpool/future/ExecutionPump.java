package org.comroid.dreadpool.future;

import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pipe.StageAdapter;
import org.comroid.mutatio.pump.BasicPump;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.time.Instant.now;

public final class ExecutionPump<T> extends BasicPump<T, T> implements ExecutionFuture<Pipe<T>> {
    private final Instant targetTime;
    private final AtomicBoolean isCancelled;

    @Override
    public boolean isCancelled() {
        return isCancelled.get();
    }

    @Override
    public boolean isDone() {
        return targetTime.isBefore(now());
    }

    @Override
    public Instant getTargetTime() {
        return targetTime;
    }

    public ExecutionPump(long targetTime, Executor executor) {
        this(targetTime, executor, null);
    }

    public ExecutionPump(long targetTime, Executor executor, Consumer<Throwable> exceptionHandler) {
        super(executor, ReferenceIndex.create(), StageAdapter.filter(any -> true), exceptionHandler);

        this.targetTime = Instant.ofEpochMilli(targetTime);
        this.isCancelled = new AtomicBoolean(false);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return isCancelled.compareAndSet(false, true);
    }

    @Override
    @Contract("-> this")
    public Pipe<T> get() {
        return this;
    }

    @Override
    @Contract("_,_ -> this")
    public Pipe<T> get(long timeout, @NotNull TimeUnit unit) {
        return get();
    }
}
