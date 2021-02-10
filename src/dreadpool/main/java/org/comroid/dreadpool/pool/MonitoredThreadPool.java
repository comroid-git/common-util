package org.comroid.dreadpool.pool;

import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class MonitoredThreadPool extends BasicThreadPool {
    private final Timer timer = new Timer();
    private final int warnTimeout;
    private final int terminateTimeout;

    public MonitoredThreadPool(ThreadGroup group, Logger logger, int maxSize, int warnTimeout, int terminateTimeout) {
        super(group, logger, maxSize * 2);

        this.warnTimeout = warnTimeout;
        this.terminateTimeout = terminateTimeout;
    }

    @Override
    protected Runnable prefabTask(Runnable fullTask) {
        return new TimeoutTask(fullTask);
    }

    private class TimeoutTask implements Runnable {
        private final Runnable task;

        public TimeoutTask(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                TimerTask warn = new TimerTask() {
                    @Override
                    public void run() {
                        logger.warn("Thread {} - Warning: Execution <{}> is not responding for {} seconds...",
                                Thread.currentThread().getName(), task.getClass().getSimpleName(), warnTimeout);
                    }
                };
                timer.schedule(warn, TimeUnit.SECONDS.toMillis(warnTimeout));
                CompletableFuture.supplyAsync(() -> {
                    task.run();
                    return null;
                }, MonitoredThreadPool.this)
                        .get(terminateTimeout, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException | TimeoutException e) {
                logger.error("Thread {} - Stopped Execution <{}> as it exceeded timeout ({} seconds)",
                        Thread.currentThread().getName(), task.getClass().getSimpleName(), terminateTimeout);
            }
        }
    }
}
