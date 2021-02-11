package org.comroid.dreadpool.pool;

import org.apache.logging.log4j.Logger;
import org.comroid.dreadpool.future.ExecutionFuture;
import org.comroid.dreadpool.future.ExecutionPump;
import org.comroid.dreadpool.future.ScheduledCompletableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

public abstract class AbstractThreadPool<W extends Worker> implements ThreadPool {
    protected final Logger logger;
    private final ThreadGroup group;
    private final int maxSize;
    private final Thread clock;
    private final AtomicBoolean isShuttingDown;
    private final AtomicBoolean isTerminated;
    private final PriorityBlockingQueue<BoxedTask> tasks;
    private final PriorityQueue<W> workers;

    @Override
    public final boolean isShutdown() {
        return isShuttingDown.get();
    }

    @Override
    public final boolean isTerminated() {
        return isTerminated.get();
    }

    @Override
    public final ThreadGroup getThreadGroup() {
        return group;
    }

    @Override
    public final int getMaximumSize() {
        return maxSize;
    }

    @NotNull
    private List<Runnable> getAwaitingExecutionTasks() {
        return workers.stream()
                .flatMap(Worker::streamWork)
                .collect(Collectors.toList());
    }

    public AbstractThreadPool(ThreadGroup group, int maxSize) {
        this(group, null, maxSize);
    }

    public AbstractThreadPool(ThreadGroup group, Logger logger, int maxSize) {
        this.group = group;
        this.logger = logger;
        this.maxSize = maxSize;
        this.clock = new Thread(group, new ClockTask());
        this.isShuttingDown = new AtomicBoolean(false);
        this.isTerminated = new AtomicBoolean(false);
        this.tasks = new PriorityBlockingQueue<>(1, BoxedTask.COMPARATOR);
        this.workers = new PriorityQueue<>(Worker.COMPARATOR);

        clock.start();
    }

    @Override
    public final void execute(@NotNull Runnable command) {
        W worker = null;
        if (workers.size() < maxSize && allBusy()) {
            worker = createWorker();
            if (worker == null)
                throw new RuntimeException("Unable to create new Worker");
            workers.add(worker);
        }
        while (worker == null || worker.isBusy())
            worker = workers.peek();
        worker.accept(prefabTask(command));
    }

    private boolean allBusy() {
        return workers.stream().allMatch(Worker::isBusy);
    }

    @Override
    public final Consumer<Throwable> getExceptionHandler(final String message) {
        if (logger == null)
            throw new AbstractMethodError();
        return thr -> logger.error(message, thr);
    }

    @Override
    public final void shutdown() {
        isShuttingDown.set(true);
        isTerminated.set(false);
        clock.stop();
        tasks.clear();
        workers.forEach(Worker::close);
    }

    @NotNull
    @Override
    public final List<Runnable> shutdownNow() {
        isShuttingDown.set(true);
        isTerminated.set(false);
        clock.stop();
        tasks.clear();
        List<Runnable> awaited = getAwaitingExecutionTasks();
        workers.forEach(Worker::close);
        return awaited;
    }

    @Override
    public final boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        try {
            CompletableFuture.supplyAsync(this::shutdownNow)
                    .thenAccept(tasks -> tasks.forEach(Runnable::run))
                    .get(timeout, unit);
            return true;
        } catch (ExecutionException e) {
            throw new InterruptedException("Interrupted by Exception: " + e.toString());
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public final @NotNull <V> ScheduledCompletableFuture<V> schedule(@NotNull Callable<V> command, long delay, @NotNull TimeUnit unit) {
        return queueTask(new BoxedTask.Simple<>(this, delay, unit, command));
    }

    @Override
    public final @NotNull <R> ExecutionPump<R> scheduleAtFixedRate(@NotNull Callable<R> command, long initialDelay, long rate, @NotNull TimeUnit unit) {
        return queueTask(new BoxedTask.FixedRate<>(this, initialDelay, rate, unit, command));
    }

    @Override
    public final @NotNull <R> ExecutionPump<R> scheduleWithFixedDelay(@NotNull Callable<R> command, long initialDelay, long delay, @NotNull TimeUnit unit) {
        return queueTask(new BoxedTask.FixedDelay<>(this, initialDelay, delay, unit, command));
    }

    protected abstract W createWorker();

    protected abstract Runnable prefabTask(Runnable fullTask);

    @NotNull
    private <T, EF extends ExecutionFuture<T>> EF queueTask(BoxedTask<T, EF> task) {
        if (isShuttingDown.get())
            throw new IllegalStateException("ThreadPool is shutting down");

        synchronized (tasks) {
            if (tasks.add(task))
                tasks.notify();
            return task.future;
        }
    }

    private final class ClockTask implements Runnable {
        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait(50);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Clock failed to wait", e);
                        }
                    }

                    long now = currentTimeMillis();
                    BoxedTask task;
                    while ((task = tasks.peek()) != null && task.getTargetTime() <= now)
                        if (tasks.remove(task))
                            doExecute(task, now);
                }
            }
        }

        private void doExecute(final BoxedTask task, final long time) {
            execute(() -> task.execute(time));
        }
    }
}
