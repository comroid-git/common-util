package org.comroid.dreadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.jetbrains.annotations.NotNull;

public interface ThreadPool extends ExecutorService, ScheduledExecutorService {
    @Override
    void execute(@NotNull Runnable command);
}
