package org.comroid.dreadpool;

import org.comroid.api.Polyfill;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Flushable;
import java.util.Comparator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.System.nanoTime;

public interface ThreadPool extends ExecutorService, Flushable, ScheduledExecutorService {
    WorkerFactory getThreadFactory();

    ThreadErrorHandler getThreadErrorHandler();

    static FixedSizeThreadPool fixedSize(ThreadGroup group, int corePoolSize) {
        return new FixedSizeThreadPool(corePoolSize, new WorkerFactory(group, corePoolSize), new ThreadErrorHandler());
    }

    @Override
    void execute(@NotNull Runnable command);

    @Override
    void flush();

    long queue(@NotNull Runnable runnable);

    boolean unqueue(long timestamp);

    int queueSize();

    final class Task implements Comparable<Task> {
        public static final Comparator<Task> TASK_COMPARATOR = Comparator.comparingLong(Task::getIssuedAt);
        private final long issuedAt = nanoTime();
        private final Runnable runnable;

        public long getIssuedAt() {
            return issuedAt;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public Task(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public int compareTo(@NotNull ThreadPool.Task other) {
            return TASK_COMPARATOR.compare(this, other);
        }
    }

}
