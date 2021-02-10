package org.comroid.dreadpool.pool;

import org.comroid.api.Rewrapper;
import org.comroid.api.ThrowingFunction;
import org.comroid.api.ThrowingPredicate;
import org.comroid.dreadpool.future.ExecutionPump;
import org.comroid.dreadpool.future.ScheduledCompletableFuture;
import org.comroid.mutatio.ref.FutureReference;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface ThreadPool extends ScheduledExecutorService {
    ThreadGroup getThreadGroup();

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
    default <R> ScheduledCompletableFuture<R> execute(@NotNull Callable<R> callable) {
        return schedule(callable, 0, MILLISECONDS);
    }

    @NotNull
    @Override
    default <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) {
        return tasks.stream()
                .map(this::execute)
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    default <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) {
        return invokeAll(tasks);
    }

    @NotNull
    @Override
    default <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) {
        return tasks.stream()
                .map(this::execute)
                .map(FutureReference::new)
                .filter(ThrowingPredicate.swallowing(Rewrapper::isNonNull))
                .findAny()
                .flatMap(Rewrapper::wrap)
                .orElseThrow(() -> new IllegalStateException("None of the tasks executed successfully"));
    }

    @Override
    default <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) {
        return tasks.stream()
                .map(this::execute)
                .map(ThrowingFunction.rethrowing(future -> future.get(timeout, unit), RuntimeException::new))
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("None of the tasks executed successfully"));
    }

    @NotNull
    @Override
    <R> ScheduledCompletableFuture<R> schedule(@NotNull Callable<R> callable, long delay, @NotNull TimeUnit unit);

    @NotNull
    @Override
    default ExecutionPump<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period, @NotNull TimeUnit unit) {
        return scheduleAtFixedRate(voidCallable(command), initialDelay, period, unit);
    }

    @NotNull <R> ExecutionPump<R> scheduleAtFixedRate(@NotNull Callable<R> command, long initialDelay, long period, @NotNull TimeUnit unit);

    @NotNull
    @Override
    default ExecutionPump<?> scheduleWithFixedDelay(@NotNull Runnable command, long initialDelay, long delay, @NotNull TimeUnit unit) {
        return scheduleWithFixedDelay(voidCallable(command), initialDelay, delay, unit);
    }

    @NotNull <R> ExecutionPump<R> scheduleWithFixedDelay(@NotNull Callable<R> command, long initialDelay, long delay, @NotNull TimeUnit unit);

    @NotNull
    @Override
    default <T> ScheduledCompletableFuture<T> submit(@NotNull Callable<T> task) {
        return schedule(task, 0, MILLISECONDS);
    }

    @NotNull
    @Override
    default <T> ScheduledCompletableFuture<T> submit(@NotNull Runnable task, final T result) {
        return schedule(() -> {
            task.run();
            return result;
        }, 0, MILLISECONDS);
    }

    @NotNull
    @Override
    default ScheduledCompletableFuture<?> submit(@NotNull Runnable task) {
        return submit(task, null);
    }

    Consumer<Throwable> getExceptionHandler(String message);
}
