package org.comroid.dreadpool;

import org.comroid.common.Polyfill;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

public interface ThreadPool extends ExecutorService, ScheduledExecutorService {
    static FixedSizeThreadPool fixedSize(ThreadGroup group, int corePoolSize) {
        return new FixedSizeThreadPool(corePoolSize, new WorkerFactory(group, corePoolSize), new ThreadErrorHandler());
    }

    @Override
    void execute(@NotNull Runnable command);

    WorkerFactory getThreadFactory();

    ThreadErrorHandler getThreadErrorHandler();

    void flush();

    long queue(@NotNull Runnable runnable);

    boolean unqueue(long timestamp);

    int queueSize();

    final class Task implements Comparable<Task> {
        public static final Comparator<Task> TASK_COMPARATOR = Comparator.comparingLong(Task::getIssuedAt);

        private final long     issuedAt = System.nanoTime();
        private final Runnable runnable;

        public Task(Runnable runnable) {
            this.runnable = runnable;
        }

        public long getIssuedAt() {
            return issuedAt;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        @Override
        public int compareTo(@NotNull ThreadPool.Task other) {
            return TASK_COMPARATOR.compare(this, other);
        }
    }

    class Worker extends org.comroid.dreadpool.Worker implements Executor, Comparable<ThreadPool.Worker> {
        public static final int                ERR_STACKSIZE     = 5;
        public static final Comparator<Worker> WORKER_COMPARATOR = Comparator.comparingLong(Worker::lastOp);

        private final Object          lock  = Polyfill.selfawareLock();
        private final Queue<Runnable> queue = new LinkedBlockingQueue<>();
        ThreadPool threadPool;
        private boolean busy     = true;
        private long    lastOp   = 0;
        private int     errStack = 0;

        protected Worker(@Nullable ThreadGroup group, @NotNull String name) {
            super(group, name);
        }

        public long lastOp() {
            return lastOp;
        }

        public boolean isBusy() {
            synchronized (lock) {
                return busy;
            }
        }

        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            busy = false;

            while (true) {
                synchronized (lock) {
                    while (queue.isEmpty()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            threadPool.getThreadErrorHandler()
                                    .handleInterrupted(e);
                        }
                    }

                    busy = true;
                    while (!queue.isEmpty())
                        queue.poll()
                                .run();
                    busy = false;
                }
            }
        }

        @Override
        public void execute(@NotNull Runnable task) {
            Objects.requireNonNull(task);

            synchronized (lock) {
                queue.add(task);
                lock.notifyAll();
            }
        }

        @Override
        public int compareTo(@NotNull ThreadPool.Worker other) {
            return WORKER_COMPARATOR.compare(this, other);
        }

        @Override
        public String toString() {
            return String.format(
                    "%s{threadPool=%s, busy=%s, lock=%s}",
                    getClass().getSimpleName(),
                    threadPool,
                    busy,
                    lock
            );
        }
    }
}
