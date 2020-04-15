package org.comroid.dreadpool;

import com.google.common.flogger.FluentLogger;
import org.comroid.dreadpool.ThreadPool;
import org.comroid.dreadpool.Worker;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class FixedSizeThreadPool<W extends Worker> extends ScheduledThreadPoolExecutor implements ThreadPool {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    FixedSizeThreadPool(
            int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler
    ) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        // todo
    }
}
