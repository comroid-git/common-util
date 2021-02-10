package org.comroid.dreadpool.pool;

import org.comroid.annotations.Blocking;
import org.comroid.dreadpool.future.ScheduledCompletableFuture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class AbstractThreadPool implements ThreadPool {
    private final ThreadGroup group;
    private final ThreadFactory threadFactory;
    private final Thread clock;
    private final int maxSize;
    private final TreeMap<Long, Collection<TaskBox>> tasks;

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

    protected abstract Runnable prefabTask(Runnable fullTask);

    private synchronized <T> TaskBox<T> queueTask(long delay, TimeUnit unit, Callable<T> task) {
        final TaskBox<T> box = new TaskBox<>(delay, unit, task);
        final Collection<TaskBox> boxes = computeList(box.execution);
        boxes.add(box);
        return box;
    }

    private Collection<TaskBox> computeList(long key) {
        return tasks.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private static final class TaskBox<T> {
        private final AtomicBoolean cancelled;
        private final long execution;
        private final Callable<T> fullTask;
        private final ScheduledCompletableFuture<T> future;

        private boolean isCancelled() {
            return cancelled.get();
        }

        private TaskBox(long delayTime, TimeUnit delayUnit, Callable<T> fullTask) {
            this.cancelled = new AtomicBoolean(false);
            this.execution = currentTimeMillis() + MILLISECONDS.convert(delayTime, delayUnit);
            this.fullTask = fullTask;
            this.future = new ScheduledCompletableFuture<>(execution, () -> cancelled.compareAndSet(false, true));
        }

        @Blocking
        private void execute(final long time) {
            if (future.isDone())
                throw new IllegalStateException("Task has already been executed!");
            if (execution > time || isCancelled())
                return;
            T yield = null;
            try {
                yield = fullTask.call();
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            } finally {
                future.complete(yield);
            }
        }
    }

    private final class ClockTask implements Runnable {
        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                final long now = currentTimeMillis();

                Collection<TaskBox> boxes;
                while (tasks.firstKey() <= now) {
                    boxes = tasks.pollFirstEntry().getValue();

                    if (boxes.size() == 0)
                        continue;
                    for (TaskBox task : boxes)
                        execute(() -> task.execute(now));
                }
            }
        }
    }
}
