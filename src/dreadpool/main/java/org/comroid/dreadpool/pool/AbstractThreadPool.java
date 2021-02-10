package org.comroid.dreadpool.pool;

import org.comroid.dreadpool.future.ExecutionFuture;
import org.comroid.dreadpool.future.ExecutionPump;
import org.comroid.dreadpool.future.ScheduledCompletableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

public abstract class AbstractThreadPool implements ThreadPool {
    private final ThreadGroup group;
    private final ThreadFactory threadFactory;
    private final Thread clock;
    private final int maxSize;
    private final TreeMap<Long, Collection<BoxedTask>> tasks;

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
        this.tasks = new TreeMap<>(Comparator.naturalOrder());

        clock.start();
    }

    @Override
    public @NotNull <V> ScheduledCompletableFuture<V> schedule(@NotNull Callable<V> command, long delay, @NotNull TimeUnit unit) {
        return addBox(new BoxedTask.Simple<>(this, delay, unit, command)).future;
    }

    @Override
    public @NotNull <R> ExecutionPump<R> scheduleAtFixedRate(@NotNull Callable<R> command, long initialDelay, long rate, @NotNull TimeUnit unit) {
        return addBox(new BoxedTask.FixedRate<>(this, initialDelay, rate, unit, command)).future;
    }

    @Override
    public @NotNull <R> ExecutionPump<R> scheduleWithFixedDelay(@NotNull Callable<R> command, long initialDelay, long delay, @NotNull TimeUnit unit) {
        return addBox(new BoxedTask.FixedDelay<>(this, initialDelay, delay, unit, command)).future;
    }

    protected abstract Runnable prefabTask(Runnable fullTask);

    @NotNull
    private <T, EF extends ExecutionFuture<T>> BoxedTask<T, EF> addBox(BoxedTask<T, EF> box) {
        Collection<BoxedTask> boxes = computeList(box.getTargetTime());
        boxes.add(box);
        return box;
    }

    private Collection<BoxedTask> computeList(long key) {
        return tasks.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private final class ClockTask implements Runnable {
        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                final long now = currentTimeMillis();

                Collection<BoxedTask> boxes;
                while (tasks.firstKey() <= now) {
                    boxes = tasks.pollFirstEntry().getValue();

                    if (boxes.size() == 0)
                        continue;
                    for (BoxedTask task : boxes)
                        execute(() -> task.execute(now));
                }
            }
        }
    }
}
