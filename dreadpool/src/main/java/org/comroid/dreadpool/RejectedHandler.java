package org.comroid.dreadpool;

import com.google.common.flogger.FluentLogger;
import org.comroid.dreadpool.ThreadPool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;

public class RejectedHandler implements RejectedExecutionHandler {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        if (executor instanceof ThreadPool) {
            logger.at(Level.WARNING)
                    .log("Rescheduling rejected runnable %s into executor %s", task, executor);
            executor.execute(task);
        } else
            logger.at(Level.WARNING)
                    .log(
                            "Ignored suspicious RejectedExecutionEvent; didnt come from %s",
                            ThreadPool.class.getName()
                    );
    }
}
