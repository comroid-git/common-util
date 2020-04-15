package org.comroid.dreadpool;

import com.google.common.flogger.FluentLogger;
import org.comroid.common.func.Factory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadFactory;

public class WorkerFactory<W> implements Factory<W>, ThreadFactory {
    private static final FluentLogger logger  = FluentLogger.forEnclosingClass();

    private final        Map<Long, W> workers = new TreeMap<>();
    private final int          maxSize;
    private       int          c       = 0;

    public WorkerFactory(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public int counter() {
        return c++;
    }

    @Override
    public W create() {
        if (counter() >= maxSize)
            return null;

        return null;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        return null;
    }
}
