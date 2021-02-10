package org.comroid.dreadpool.pool;

import org.comroid.dreadpool.future.ExecutionPump;
import org.comroid.dreadpool.future.ScheduledCompletableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface ThreadPool extends ScheduledExecutorService {
    ThreadGroup getThreadGroup();

    ThreadFactory getThreadFactory();

    int getMaximumSize();

    static Callable<Void> voidCallable(final Runnable command) {
        return () -> {
            command.run();
            return null;
        };
    }

    @NotNull
    @Override
    default ScheduledCompletableFuture<?> schedule(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
        return schedule(voidCallable(command), delay, unit);
    }

    @NotNull
    @Override
    <R> ScheduledCompletableFuture<R> schedule(@NotNull Callable<R> callable, long delay, @NotNull TimeUnit unit);

    @NotNull
    @Override
    default ExecutionPump<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period, @NotNull TimeUnit unit) {
        return scheduleAtFixedRate(voidCallable(command), initialDelay, period, unit);
    }

    @NotNull
    <R> ExecutionPump<R> scheduleAtFixedRate(@NotNull Callable<R> command, long initialDelay, long period, @NotNull TimeUnit unit);

    @NotNull
    @Override
    default ExecutionPump<?> scheduleWithFixedDelay(@NotNull Runnable command, long initialDelay, long delay, @NotNull TimeUnit unit) {
        return scheduleWithFixedDelay(voidCallable(command), initialDelay, delay, unit);
    }

    @NotNull
    <R> ExecutionPump<R> scheduleWithFixedDelay(@NotNull Callable<R> command, long initialDelay, long delay, @NotNull TimeUnit unit);

    @NotNull
    @Override
    <T> ScheduledCompletableFuture<T> submit(@NotNull Callable<T> task);

    @NotNull
    @Override
    <T> ScheduledCompletableFuture<T> submit(@NotNull Runnable task, T result);

    @NotNull
    @Override
    ScheduledCompletableFuture<?> submit(@NotNull Runnable task);

    Consumer<Throwable> getExceptionHandler(String name);

    @Override
    default void execute(@NotNull Runnable command) {
        schedule(ThreadPool.voidCallable(command), 0, MILLISECONDS);
    }
}
