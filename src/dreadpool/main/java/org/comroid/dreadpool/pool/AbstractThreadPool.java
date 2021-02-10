package org.comroid.dreadpool.pool;

import org.comroid.dreadpool.future.ExecutionFuture;
import org.comroid.dreadpool.future.ExecutionPump;
import org.comroid.dreadpool.future.ScheduledCompletableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

public abstract class AbstractThreadPool implements ThreadPool {
    private final ThreadGroup group;
    private final ThreadFactory threadFactory;
    private final Thread clock;
    private final int maxSize;
    private final PriorityBlockingQueue<BoxedTask> tasks;

    @Override
    public final ThreadGroup getThreadGroup() {
        return group;
    }

    @Override
    public final ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    @Override
    public int getMaximumSize() {
        return maxSize;
    }

    public AbstractThreadPool(ThreadGroup group, ThreadFactory threadFactory, int maxSize) {
        this.group = group;
        this.threadFactory = threadFactory;
        this.clock = new Thread(group, new ClockTask());
        this.maxSize = maxSize;
        this.tasks = new PriorityBlockingQueue<>(0, BoxedTask.COMPARATOR);

        clock.start();
    }

    @Override
    public @NotNull <V> ScheduledCompletableFuture<V> schedule(@NotNull Callable<V> command, long delay, @NotNull TimeUnit unit) {
        return queueTask(new BoxedTask.Simple<>(this, delay, unit, command));
    }

    @Override
    public @NotNull <R> ExecutionPump<R> scheduleAtFixedRate(@NotNull Callable<R> command, long initialDelay, long rate, @NotNull TimeUnit unit) {
        return queueTask(new BoxedTask.FixedRate<>(this, initialDelay, rate, unit, command));
    }

    @Override
    public @NotNull <R> ExecutionPump<R> scheduleWithFixedDelay(@NotNull Callable<R> command, long initialDelay, long delay, @NotNull TimeUnit unit) {
        return queueTask(new BoxedTask.FixedDelay<>(this, initialDelay, delay, unit, command));
    }

    protected abstract Runnable prefabTask(Runnable fullTask);

    @NotNull
    private <T, EF extends ExecutionFuture<T>> EF queueTask(BoxedTask<T, EF> task) {
        synchronized (tasks) {
            if (tasks.add(task))
                tasks.notify();
            return task.future;
        }
    }

    private final class ClockTask implements Runnable {
        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Clock failed to wait", e);
                        }
                    }

                    long now = currentTimeMillis();
                    BoxedTask task;
                    while ((task = tasks.peek()) != null && task.getTargetTime() <= now)
                        if (tasks.remove(task))
                            doExecute(task, now);
                }
            }
        }

        private void doExecute(final BoxedTask task, final long time) {
            execute(() -> task.execute(time));
        }
    }
}
