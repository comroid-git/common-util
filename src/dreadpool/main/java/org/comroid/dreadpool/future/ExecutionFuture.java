package org.comroid.dreadpool.future;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface ExecutionFuture<T> extends ScheduledFuture<T> {
    Comparator<Delayed> COMPARATOR = Comparator.comparingLong(it -> it.getDelay(MILLISECONDS));

    Instant getTargetTime();

    default Duration getRemainingDuration() {
        return Duration.between(now(), getTargetTime());
    }

    @Override
    default long getDelay(@NotNull TimeUnit unit) {
        return unit.convert(getRemainingDuration().toMillis(), MILLISECONDS);
    }

    @Override
    default int compareTo(@NotNull Delayed other) {
        return COMPARATOR.compare(this, other);
    }
}
