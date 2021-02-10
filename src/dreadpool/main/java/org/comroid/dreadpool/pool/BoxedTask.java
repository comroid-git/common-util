package org.comroid.dreadpool.pool;

import org.comroid.annotations.Blocking;
import org.comroid.dreadpool.future.ExecutionFuture;
import org.comroid.dreadpool.future.ExecutionPump;
import org.comroid.dreadpool.future.ScheduledCompletableFuture;
import org.comroid.mutatio.ref.Reference;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Internal
public abstract class BoxedTask<T, EF extends ExecutionFuture<T>> {
    public static final Comparator<BoxedTask> COMPARATOR = Comparator.comparingLong(BoxedTask::getTargetTime);
    protected final ThreadPool pool;
    protected final AtomicBoolean cancelled;
    protected final Callable<T> fullTask;
    final EF future;

    protected boolean isCancelled() {
        return cancelled.get();
    }

    protected abstract boolean isDue();

    public long getTargetTime() {
        return future.getTargetTime().toEpochMilli();
    }

    protected BoxedTask(ThreadPool pool, Callable<T> fullTask) {
        this.pool = pool;
        this.cancelled = new AtomicBoolean(false);
        this.fullTask = fullTask;
        this.future = createFuture();
    }

    @Internal
    protected abstract EF createFuture();

    @Blocking
    void execute(final long time) {
        if (!isDue() || isCancelled())
            return;
        T yield = null;
        try {
            yield = fullTask.call();
        } catch (Exception ex) {
            future.pushException(ex);
        } finally {
            future.pushValue(yield);
        }
    }

    @Internal
    public static final class Simple<T> extends BoxedTask<T, ScheduledCompletableFuture<T>> {
        private final long execution;

        @Override
        protected boolean isDue() {
            return execution < currentTimeMillis();
        }

        Simple(ThreadPool pool, long delay, TimeUnit unit, Callable<T> fullTask) {
            super(pool, fullTask);

            this.execution = currentTimeMillis() + MILLISECONDS.convert(delay, unit);
        }

        @Override
        protected ScheduledCompletableFuture<T> createFuture() {
            return new ScheduledCompletableFuture<>(execution, () -> cancelled.compareAndSet(false, true), cancelled::get);
        }
    }

    public abstract static class Repeating<T> extends BoxedTask<T, ExecutionPump<T>> {
        protected final TimeUnit unit;
        protected final Reference<Long> lastExecution;
        protected final Reference<Long> nextExecution;

        @Override
        protected boolean isDue() {
            return nextExecution.test(x -> x <= unit.convert(currentTimeMillis(), MILLISECONDS));
        }

        @Override
        public long getTargetTime() {
            return nextExecution.assertion();
        }

        public Repeating(ThreadPool pool, Callable<T> fullTask, long initialDelay, long eachDelay, TimeUnit unit) {
            super(pool, fullTask);

            long initialMs = MILLISECONDS.convert(initialDelay, unit);
            final long eachMs = MILLISECONDS.convert(eachDelay, unit);

            this.unit = unit;
            this.lastExecution = Reference.create(currentTimeMillis() + (initialMs - eachMs));
            this.nextExecution = lastExecution.map(x -> x + eachMs);
        }
    }

    @Internal
    public static final class FixedRate<T> extends Repeating<T> {
        FixedRate(ThreadPool pool, long initialDelay, long fixedRate, TimeUnit unit, Callable<T> fullTask) {
            super(pool, fullTask, initialDelay, fixedRate, unit);
        }

        @Override
        protected ExecutionPump<T> createFuture() {
            return new ExecutionPump<>(this, pool, cancelled, pool.getExceptionHandler("ExceptionPump"));
        }

        @Override
        void execute(long time) {
            lastExecution.set(time);
            super.execute(time);
        }
    }

    @Internal
    public static final class FixedDelay<T> extends Repeating<T> {
        FixedDelay(ThreadPool pool, long initialDelay, long fixedDelay, TimeUnit unit, Callable<T> fullTask) {
            super(pool, fullTask, initialDelay, fixedDelay, unit);
        }

        @Override
        protected ExecutionPump<T> createFuture() {
            return new ExecutionPump<>(this, pool, cancelled, pool.getExceptionHandler("ExceptionPump"));
        }

        @Override
        void execute(long time) {
            super.execute(time);
            lastExecution.set(currentTimeMillis());
        }
    }
}
