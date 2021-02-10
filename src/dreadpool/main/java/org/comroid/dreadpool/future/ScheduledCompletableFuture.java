package org.comroid.dreadpool.future;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

public final class ScheduledCompletableFuture<T> extends CompletableFuture<T> implements ExecutionFuture<T> {
    private final Instant targetTime;
    private final BooleanSupplier cancellation;
    private final BooleanSupplier isCancelled;

    @Override
    public boolean isCancelled() {
        return isCancelled.getAsBoolean() || super.isCancelled();
    }

    @Override
    public Instant getTargetTime() {
        return targetTime;
    }

    public ScheduledCompletableFuture(long targetTime, BooleanSupplier cancellation, BooleanSupplier isCancelled) {
        this.targetTime = Instant.ofEpochMilli(targetTime);
        this.cancellation = cancellation;
        this.isCancelled = isCancelled;
    }

    @Override
    public void pushValue(T value) {
        validateUndone();
        complete(value);
    }

    @Override
    public void pushException(Exception ex) {
        validateUndone();
        completeExceptionally(ex);
    }

    private void validateUndone() {
        if (isDone())
            throw new IllegalStateException("Task has already been executed!");
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return cancellation.getAsBoolean();
    }
}
