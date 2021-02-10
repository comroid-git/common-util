package org.comroid.dreadpool.pool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.comroid.api.IntEnum;
import org.comroid.api.Rewrapper;
import org.comroid.api.UncheckedCloseable;
import org.comroid.mutatio.ref.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.System.nanoTime;

public abstract class Worker implements Consumer<Runnable>, UncheckedCloseable {
    public static final Comparator<Worker> COMPARATOR = Comparator.comparing(Worker::getWorkerState);
    private static final Logger logger = LogManager.getLogger();
    private final Reference<State> state;
    private final PriorityBlockingQueue<WorkerTask> work;
    private final Thread thread;

    public Worker.State getWorkerState() {
        return state.assertion("State not found");
    }

    public boolean isBusy() {
        return getWorkerState() == State.WORKING;
    }

    public Worker(ThreadGroup group, String name) {
        this.thread = new Thread(group, new WorkerClock(), name);
        this.state = Reference.create(State.IDLE);
        this.work = new PriorityBlockingQueue<>(0, WorkerTask.COMPARATOR);

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
            //noinspection InfiniteLoopStatement
            while (true) {
                synchronized (work) {
                    while (work.isEmpty()) {
                        try {
                            work.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Worker failed to wait", e);
                        }
                    }

                    while (!work.isEmpty())
                        work.poll().run();
                }
            }
        }
    }
}
