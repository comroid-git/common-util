package org.comroid.dreadpool.pool;

import org.comroid.dreadpool.future.ScheduledCompletableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public interface ThreadPool extends ScheduledExecutorService {
    ThreadGroup getThreadGroup();

    ThreadFactory getThreadFactory();

    int getMaximumSize();

    @NotNull
    @Override
    ScheduledCompletableFuture<?> schedule(@NotNull Runnable command, long delay, @NotNull TimeUnit unit);

    @NotNull
    @Override
    <V> ScheduledCompletableFuture<V> schedule(@NotNull Callable<V> callable, long delay, @NotNull TimeUnit unit);

    @NotNull
    @Override
    ScheduledCompletableFuture<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period, @NotNull TimeUnit unit);

    @NotNull
    @Override
    ScheduledCompletableFuture<?> scheduleWithFixedDelay(@NotNull Runnable command, long initialDelay, long delay, @NotNull TimeUnit unit);

    @NotNull
    @Override
    <T> ScheduledCompletableFuture<T> submit(@NotNull Callable<T> task);

    @NotNull
    @Override
    <T> ScheduledCompletableFuture<T> submit(@NotNull Runnable task, T result);

    @NotNull
    @Override
    ScheduledCompletableFuture<?> submit(@NotNull Runnable task);
}
