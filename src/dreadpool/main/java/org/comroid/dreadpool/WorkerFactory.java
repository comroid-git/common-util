package org.comroid.dreadpool;

import org.comroid.api.Factory;
import org.comroid.api.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class WorkerFactory implements Executor, Factory<ThreadPoolWorker>, ThreadFactory {
    private final Queue<ThreadPoolWorker> workers = new PriorityQueue<>();
    private final Provider<ThreadPoolWorker> workerProvider;
    private final int maxSize;
    public ThreadPool threadPool = null;
    private int c = 0;

    public WorkerFactory(ThreadGroup group, int maxSize) {
        this(() -> new ThreadPoolWorker(group, "worker"), maxSize);
    }

    public WorkerFactory(Provider.Now<ThreadPoolWorker> workerProvider, int maxSize) {
        this.workerProvider = workerProvider;
        this.maxSize = maxSize;
    }

    @Override
    public ThreadPoolWorker newThread(@NotNull Runnable task) {
        final ThreadPoolWorker newWorker = create();

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
        ThreadPoolWorker worker = null;
        if (allBusy() && workers.size() < maxSize && (worker = create()) == null) {
            throw new IllegalThreadStateException("Could not create worker even though\n" + "- all workers are busy, and\n" +
                    "- there's less workers than allowed, and\n" + "- worker creation failed\n" + "\t");
        }
        if (worker != null && !worker.isAlive()) {
            worker.start();
        }

        workers.peek()
                .execute(task);
    }

    @Override
    public int counter() {
        return c > maxSize ? maxSize : c++;
    }

    public boolean allBusy() {
        return workers.stream()
                .allMatch(ThreadPoolWorker::isBusy);
    }

    /**
     * Tries to create an unstarted Worker thread.
     *
     * @return A new unstarted Worker thread, or {@code null} if no worker thread could be created.
     */
    @Override
    @Nullable
    public ThreadPoolWorker create() {
        if (counter() < maxSize) {
            ThreadPoolWorker newWorker = workerProvider.now();
            newWorker.threadPool = this.threadPool;
            workers.add(newWorker);
        } else if (allBusy()) {
            return null;
        }

        return workers.peek();
    }

    @Override
    public int peekCounter() {
        return Math.min(c, maxSize);
    }

    @Override
    public String toString() {
        return String.format("%s{threadPool=%s, maxSize=%d}", getClass().getSimpleName(), threadPool, maxSize);
    }
}
