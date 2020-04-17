package org.comroid.dreadpool;

import org.comroid.common.func.Factory;
import org.comroid.common.func.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class WorkerFactory implements Executor, Factory<ThreadPool.Worker>, ThreadFactory {
    private final Queue<ThreadPool.Worker>    workers    = new PriorityQueue<>();
    private final Provider<ThreadPool.Worker> workerProvider;
    private final int                         maxSize;
    public        ThreadPool                  threadPool = null;
    private       int                         c          = 0;

    public WorkerFactory(ThreadGroup group, int maxSize) {
        this(() -> new ThreadPool.Worker(group, "worker"), maxSize);
    }

    public WorkerFactory(Provider.Now<ThreadPool.Worker> workerProvider, int maxSize) {
        this.workerProvider = workerProvider;
        this.maxSize        = maxSize;
    }

    public boolean allBusy() {
        return workers.stream()
                .allMatch(ThreadPool.Worker::isBusy);
    }

    @Override
    public int counter() {
        return c > maxSize ? maxSize : c++;
    }

    /**
     * Tries to create an unstarted Worker thread.
     *
     * @return A new unstarted Worker thread, or {@code null} if no worker thread could be created.
     */
    @Override
    @Nullable
    public ThreadPool.Worker create() {
        if (counter() < maxSize) {
            ThreadPool.Worker newWorker = workerProvider.now();
            newWorker.threadPool = this.threadPool;
            workers.add(newWorker);
        } else if (allBusy())
            return null;

        return workers.peek();
    }

    @Override
    public ThreadPool.Worker newThread(@NotNull Runnable task) {
        final ThreadPool.Worker newWorker = create();

        if (newWorker != null) {
            newWorker.execute(task);
            return newWorker;
        } else {
            // todo: Do we need to execute here?
            execute(task);
            return null;
        }
    }

    @Override
    public void execute(@NotNull Runnable task) {
        ThreadPool.Worker worker = null;
        if (allBusy() && workers.size() < maxSize && (worker = create()) == null)
            throw new IllegalThreadStateException("Could not create worker even though\n" +
                    "- all workers are busy, and\n" +
                    "- there's less workers than allowed, and\n" +
                    "- worker creation failed\n" +
                    "\t");
        if (worker != null && !worker.isAlive())
            worker.start();

        workers.peek()
                .execute(task);
    }

    @Override
    public String toString() {
        return String.format("%s{threadPool=%s, maxSize=%d}", getClass().getSimpleName(), threadPool, maxSize);
    }
}
