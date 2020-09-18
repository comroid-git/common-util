package org.comroid.dreadpool;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class FixedSizeThreadPool extends ScheduledThreadPoolExecutor implements ThreadPool {
    private final Lock lock = new ReentrantLock();
    private final Queue<ThreadPool.Task> taskQueue = new PriorityQueue<>();

    @Override
    public final WorkerFactory getThreadFactory() {
        return (WorkerFactory) super.getThreadFactory();
    }

    @Override
    public final ThreadErrorHandler getThreadErrorHandler() {
        return (ThreadErrorHandler) super.getRejectedExecutionHandler();
    }

    FixedSizeThreadPool(
            int corePoolSize, WorkerFactory threadFactory, ThreadErrorHandler handler
    ) {
        super(corePoolSize, threadFactory, handler);

        threadFactory.threadPool = this;
    }

    @Override
    public String toString() {
        return String.format("FixedSizeThreadPool{lock=%s}", lock);
    }

    @Override
    public void execute(@NotNull Runnable task) {
        queue(task);
        flush();
    }

    @Override
    public void flush() {
        try {
            // we need the lock here
            if (!lock.tryLock()) {
                lock.lockInterruptibly();
            }
        } catch (InterruptedException e) {
            getThreadErrorHandler().handleInterrupted(e);
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
            if (!lock.tryLock()) {
                lock.lockInterruptibly();
            }
        } catch (InterruptedException e) {
            getThreadErrorHandler().handleInterrupted(e);
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
            if (!lock.tryLock()) {
                lock.lockInterruptibly();
            }
        } catch (InterruptedException e) {
            getThreadErrorHandler().handleInterrupted(e);
        } finally {
            result = taskQueue.stream()
                    .filter(task -> task.getIssuedAt() == timestamp)
                    .findAny();

            result.ifPresent(taskQueue::remove);
            lock.unlock();

        }
        return result.isPresent();
    }

    @Override
    public int queueSize() {
        return taskQueue.size();
    }
}
