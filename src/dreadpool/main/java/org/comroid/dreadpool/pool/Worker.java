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

public class Worker implements Named, Consumer<Runnable>, UncheckedCloseable, Comparable<Worker> {
    public static final Comparator<Worker> COMPARATOR = Comparator.comparing(Worker::getWorkerState).reversed();
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

    public int getQueueSize() {
        char[] chars = getName().toCharArray();
        return Integer.parseInt(String.valueOf(chars[chars.length-1]));
        //return work.size();
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

    @Override
    public String toString() {
        return String.format("Worker{%s @ <%s>[%s], %s, q=%d}",
                thread.getName(), pool.getThreadGroup().getName(), pool.getClass().getSimpleName(), getWorkerState(), getQueueSize());
    }

    @Override
    public int compareTo(@NotNull Worker other) {
        return COMPARATOR.compare(this, other);
    }

    public enum State implements IntEnum, Comparable<State> {
        IDLE,
        WORKING;

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
                            work.wait(50);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Worker failed to wait", e);
                        }
                    }

                    while (!work.isEmpty()) {
                        state.set(State.WORKING);
                        WorkerTask task = work.poll();
                        logger.trace("Worker <{}> executing task {}", threadName, task);
                        task.run();
                        state.set(State.IDLE);
                    }
                }
            }
        }
    }
}
