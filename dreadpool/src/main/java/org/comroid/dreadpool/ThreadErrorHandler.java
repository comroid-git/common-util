package org.comroid.dreadpool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;

import com.google.common.flogger.FluentLogger;

public class ThreadErrorHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        if (executor instanceof ThreadPool) {
            logger.at(Level.WARNING)
                    .log("Rescheduling rejected runnable %s into executor %s", task, executor);
            executor.execute(task);
        } else {
            logger.at(Level.WARNING)
                    .log("Ignored suspicious RejectedExecutionEvent; didnt come from %s", ThreadPool.class.getName());
        }
    }

    public void handleInterrupted(InterruptedException IEx) {
        logger.at(Level.SEVERE)
                .withCause(IEx)
                .log();
    }
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
}
