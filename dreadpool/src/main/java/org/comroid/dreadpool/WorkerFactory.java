package org.comroid.dreadpool;

import com.google.common.flogger.FluentLogger;
import org.comroid.common.func.Factory;
import org.comroid.common.func.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ThreadFactory;

public class WorkerFactory<W extends ThreadPool.Worker> implements Factory<W>, ThreadFactory {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private final Queue<W>    workers = new PriorityQueue<>();
    private final Provider<W> underlying;
    private final int         maxSize;
    public        ThreadPool  threadPool = null;
    private       int         c       = 0;

    public WorkerFactory(ThreadGroup group, int maxSize) {
        this(() -> (W) new ThreadPool.Worker(group, "worker"), maxSize);
    }

    public WorkerFactory(Provider.Now<W> underlying, int maxSize) {
        this.underlying = underlying;
        this.maxSize    = maxSize;
    }

    @Override
    public int counter() {
        return c++;
    }

    @Override
    @Nullable
    public W create() {
        if (counter() < maxSize)
            workers.add(underlying.now());

        return workers.peek();
    }

    @Override
    public W newThread(@NotNull Runnable r) {
        return null;
    }
}
