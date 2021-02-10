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

public final class ScheduledCompletableFuture<T> extends CompletableFuture<T> implements ScheduledFuture<T> {
    private static final Comparator<Delayed> COMPARATOR = Comparator.comparingLong(it -> it.getDelay(MILLISECONDS));
    private final Instant targetTime;
    private final BooleanSupplier cancellation;

    public ScheduledCompletableFuture(long targetTime, BooleanSupplier cancellation) {
        this.targetTime = Instant.ofEpochMilli(targetTime);
        this.cancellation = cancellation;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return unit.convert(Duration.between(now(), targetTime).toMillis(), MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed other) {
        return COMPARATOR.compare(this, other);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return cancellation.getAsBoolean();
    }
}
