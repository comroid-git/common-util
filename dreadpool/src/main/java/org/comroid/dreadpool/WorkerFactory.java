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

    @Override
    public int counter() {
        return c++;
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
        } else if (workers.stream()
                .allMatch(ThreadPool.Worker::isBusy))
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
        workers.peek()
                .execute(task);
    }
}
