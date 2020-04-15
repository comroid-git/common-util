package org.comroid.dreadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.jetbrains.annotations.NotNull;

public interface ThreadPool extends ExecutorService, ScheduledExecutorService {
    @Override
    void execute(@NotNull Runnable command);

    static <W extends Worker> FixedSizeThreadPool<W> fixedSize(int corePoolSize) {
        return new FixedSizeThreadPool<>(corePoolSize, new WorkerFactory<W>(corePoolSize), new RejectedHandler());
    }
}
