package org.comroid.dreadpool.future;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
    public boolean cancel(boolean mayInterruptIfRunning) {
        return cancellation.getAsBoolean();
    }
}
