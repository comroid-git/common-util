package org.comroid.dreadpool;

import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ThreadPool extends ExecutorService, ScheduledExecutorService {
    @Override
    void execute(@NotNull Runnable command);

    static <W extends Worker> FixedSizeThreadPool<W> fixedSize(ThreadGroup group, int corePoolSize) {
        return new FixedSizeThreadPool<>(corePoolSize, new WorkerFactory<>(group, corePoolSize), new RejectedHandler());
    }

    class Worker extends org.comroid.dreadpool.Worker implements Comparable<ThreadPool.Worker> {
        public static final Comparator<Worker> WORKER_COMPARATOR = Comparator.comparingLong(Worker::lastOp);

        private long lastOp = 0;

        protected Worker(@Nullable ThreadGroup group, @NotNull String name) {
            super(group, name);
        }

        public long lastOp() {
            return lastOp;
        }

        @Override
        public void run() {
            while (true) {

            }
        }

        @Override
        public int compareTo(@NotNull ThreadPool.Worker other) {
            return WORKER_COMPARATOR.compare(this, other);
        }
    }
}
