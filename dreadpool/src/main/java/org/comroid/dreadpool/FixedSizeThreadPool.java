package org.comroid.dreadpool;

import com.google.common.flogger.FluentLogger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class FixedSizeThreadPool<W extends ThreadPool.Worker> extends ScheduledThreadPoolExecutor
        implements ThreadPool {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private final Lock                   lock      = new ReentrantLock();
    private final Queue<ThreadPool.Task> taskQueue = new PriorityQueue<>();

    FixedSizeThreadPool(int corePoolSize, WorkerFactory threadFactory, RejectedHandler handler) {
        super(corePoolSize, threadFactory, handler);

        threadFactory.threadPool = this;
    }

    @Override
    public final WorkerFactory getThreadFactory() {
        return (WorkerFactory) super.getThreadFactory();
    }

    @Override
    public final RejectedHandler getRejectedExecutionHandler() {
        return (RejectedHandler) super.getRejectedExecutionHandler();
    }

    @Override
    public void execute(@NotNull Runnable task) {
        queue(task);
        flush();
    }

    @Override
    public void handleInterrupted(InterruptedException IEx) {
        logger.at(Level.SEVERE)
                .withCause(IEx)
                .log();
    }

    @Override
    public void flush() {
        try {
            // we need the lock here
            if (!lock.tryLock())
                lock.lockInterruptibly();
        } catch (InterruptedException e) {
            handleInterrupted(e);
        } finally {
            taskQueue.stream()
                    .map(Task::getRunnable)
                    .forEachOrdered(getThreadFactory()::execute);
            taskQueue.clear();

            lock.unlock();
        }
    }

    @Override
    public long queue(@NotNull Runnable runnable) {
        Task task;

        try {
            // we need the lock here
            if (!lock.tryLock())
                lock.lockInterruptibly();
        } catch (InterruptedException e) {
            handleInterrupted(e);
        } finally {
            task = new Task(runnable);
            taskQueue.add(task);

            lock.unlock();
        }

        return task.getIssuedAt();
    }

    @Override
    public boolean unqueue(long timestamp) {
        Optional<Task> result;

        try {
            // we need the lock here
            if (!lock.tryLock())
                lock.lockInterruptibly();
        } catch (InterruptedException e) {
            handleInterrupted(e);
        } finally {
            result = taskQueue.stream()
                    .filter(task -> task.getIssuedAt() == timestamp)
                    .findAny();

            result.ifPresent(taskQueue::remove);
            lock.unlock();

        }
        return result.isPresent();
    }
}
