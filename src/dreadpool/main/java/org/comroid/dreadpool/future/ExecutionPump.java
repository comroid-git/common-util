package org.comroid.dreadpool.future;

import org.comroid.dreadpool.pool.BoxedTask;
import org.comroid.mutatio.pipe.StageAdapter;
import org.comroid.mutatio.pump.BasicPump;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class ExecutionPump<T> extends BasicPump<T, T> implements ExecutionFuture<T> {
    private final BoxedTask.Repeating<T> task;
    private final AtomicBoolean isCancelled;

    @Override
    public boolean isCancelled() {
        return isCancelled.get();
    }

    @Override
    public boolean isDone() {
        return isCancelled();
    }

    @Override
    public Instant getTargetTime() {
        return Instant.ofEpochMilli(task.getTargetTime());
    }

    public ExecutionPump(BoxedTask.Repeating<T> task, Executor executor, AtomicBoolean isCancelled, Consumer<Throwable> exceptionHandler) {
        super(executor, ReferenceIndex.create(), StageAdapter.filter(any -> true), exceptionHandler);

        this.task = task;
        this.isCancelled = isCancelled;
    }

    @Override
    public void pushValue(T value) {
        accept(Reference.constant(value));
    }

    @Override
    public void pushException(Exception ex) {
        getExceptionHandler().accept(ex);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return isCancelled.compareAndSet(false, true);
    }

    @Override
    public void close() throws IOException {
        cancel(true);
        super.close();
    }

    @Override
    public T get() {
        throw new UnsupportedOperationException("ExecutionPump must be consumed directly");
    }

    @Override
    public T get(long timeout, @NotNull TimeUnit unit) {
        return get();
    }
}
