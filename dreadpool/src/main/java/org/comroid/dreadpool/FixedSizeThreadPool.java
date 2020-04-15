package org.comroid.dreadpool;

import com.google.common.flogger.FluentLogger;
import org.comroid.dreadpool.ThreadPool;
import org.comroid.dreadpool.Worker;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class FixedSizeThreadPool<W extends ThreadPool.Worker> extends ScheduledThreadPoolExecutor implements ThreadPool {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    FixedSizeThreadPool(int corePoolSize, WorkerFactory<W> threadFactory, RejectedHandler handler) {
        super(corePoolSize, threadFactory, handler);

        threadFactory.threadPool = this;
    }

    @Override
    public final WorkerFactory<W> getThreadFactory() {
        return (WorkerFactory<W>) super.getThreadFactory();
    }

    @Override
    public final RejectedHandler getRejectedExecutionHandler() {
        return (RejectedHandler) super.getRejectedExecutionHandler();
    }

    @Override
    public void execute(@NotNull Runnable command) {
        // todo
    }
}
