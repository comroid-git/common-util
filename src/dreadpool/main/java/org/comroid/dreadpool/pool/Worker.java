package org.comroid.dreadpool.pool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.comroid.api.IntEnum;
import org.comroid.api.Named;
import org.comroid.api.Rewrapper;
import org.comroid.api.UncheckedCloseable;
import org.comroid.mutatio.ref.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.System.nanoTime;

public class Worker implements Named, Consumer<Runnable>, UncheckedCloseable {
    public static final Comparator<Worker> COMPARATOR = Comparator.comparing(Worker::getWorkerState);
    private static final Logger logger = LogManager.getLogger();
    private final ThreadPool pool;
    private final Thread thread;
    private final Reference<State> state;
    private final PriorityBlockingQueue<WorkerTask> work;

    public Worker.State getWorkerState() {
        return state.assertion("State not found");
    }

    public boolean isBusy() {
        return getWorkerState() == State.WORKING;
    }

    @Override
    public String getName() {
        return thread.getName();
    }

    public Worker(ThreadPool pool, String name) {
        this.pool = pool;
        this.thread = new Thread(pool.getThreadGroup(), new WorkerClock(), name);
        this.state = Reference.create(State.IDLE);
        this.work = new PriorityBlockingQueue<>(1, WorkerTask.COMPARATOR);

        thread.start();
    }

    @Override
    public void close() {
        work.clear();
        thread.stop();
    }

    @Override
    public void accept(Runnable runnable) {
        synchronized (work) {
            WorkerTask task = new WorkerTask(nanoTime(), runnable);
            work.add(task);
            work.notify();
        }
    }

    public Stream<? extends Runnable> streamWork() {
        return work.stream();
    }

    public enum State implements IntEnum, Comparable<State> {
        IDLE(0),
        WORKING(1);

        private final int value;

        @Override
        public @NotNull Integer getValue() {
            return value;
        }

        State(int value) {
            this.value = value;
        }

        public static Rewrapper<State> valueOf(int value) {
            return IntEnum.valueOf(value, State.class);
        }
    }

    private final class WorkerClock implements Runnable {
        @Override
        public void run() {
            final String threadName = Thread.currentThread().getName();
            //noinspection InfiniteLoopStatement
            while (true) {
                synchronized (work) {
                    while (work.isEmpty()) {
                        try {
                            logger.debug("WorkerClock.run - wait");
                            work.wait(50);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Worker failed to wait", e);
                        }
                    }

                    while (!work.isEmpty()) {
                        state.set(State.WORKING);
                        logger.debug("WorkerClock.run - poll");
                        WorkerTask task = work.poll();
                        logger.trace("Worker <{}> executing task {}", threadName, task);
                        logger.debug("WorkerClock.run - run");
                        task.run();
                        state.set(State.IDLE);
                    }
                }
            }
        }
    }
}
