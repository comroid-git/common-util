package org.comroid.dreadpool;

import org.comroid.api.Polyfill;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.System.nanoTime;

public class ThreadPoolWorker extends Worker implements Executor, Comparable<ThreadPoolWorker> {
    public static final int ERR_STACKSIZE = 5;
    public static final Comparator<ThreadPoolWorker> WORKER_COMPARATOR = Comparator.comparingLong(ThreadPoolWorker::lastOp);
    private final Object lock = Polyfill.selfawareLock();
    private final Queue<Runnable> queue = new LinkedBlockingQueue<>();
    private final int errStack = 0;
    ThreadPool threadPool;
    private boolean busy = true;
    private long lastOp = 0;

    public boolean isBusy() {
        synchronized (lock) {
            return busy;
        }
    }

    protected ThreadPoolWorker(@Nullable ThreadGroup group, @NotNull String name) {
        super(group, name);
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
                while (!queue.isEmpty()) {
                    final Runnable poll = queue.poll();
                    if (poll instanceof Thread)
                        if (!((Thread) poll).isAlive())
                            ((Thread) poll).start();
                        else poll.run();
                }
                lastOp = nanoTime();
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
    public int compareTo(@NotNull ThreadPoolWorker other) {
        return WORKER_COMPARATOR.compare(this, other);
    }

    @Override
    public String toString() {
        return String.format("%s{threadPool=%s, busy=%s, lock=%s}", getClass().getSimpleName(), threadPool, busy, lock);
    }

    public long lastOp() {
        return lastOp;
    }
}
