package org.comroid.dreadpool.pool;

import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class BasicThreadPool extends AbstractThreadPool<Worker> {
    private final AtomicInteger counter = new AtomicInteger(0);

    public BasicThreadPool(ThreadGroup group, int maxSize) {
        super(group, maxSize);
    }

    public BasicThreadPool(ThreadGroup group, Logger logger, int maxSize) {
        super(group, logger, maxSize);
    }

    @Override
    protected final synchronized Worker createWorker() {
        return new Worker(this, String.format("%s-worker-%d", getThreadGroup().getName(), counter.incrementAndGet()));
    }

    @Override
    protected Runnable prefabTask(Runnable fullTask) {
        return fullTask;
    }
}
