package org.comroid.dreadpool.model;

import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

public final class LoopManager {
    public static final ThreadGroup THREAD_GROUP = new ThreadGroup("LoopManager");

    public static LoopManager start(int parallelism) {
        return start(parallelism, THREAD_GROUP);
    }

    public static LoopManager start(int parallelism, @Nullable ThreadGroup group) {
        final LoopManager manager = new LoopManager();

        IntStream.range(0, parallelism + 1)
                 .mapToObj(iter -> new LoopWorker(
                         manager,
                         group,
                         String.format("LoopWorker @ %s#%4.0d", manager.toString(), iter)
                 ))
                 .forEach(Thread::start);

        return manager;
    }

    @Override
    public String toString() {
        return String.format("LoopManager{loops=%s}", loops);
    }

    final         Object         lock  = new Object() {
        @SuppressWarnings("FieldMayBeFinal")
        private volatile Object selfaware_keepalive = LoopManager.this.lock;
    };
    private final Queue<Loop<?>> loops = new PriorityQueue<>();

    private LoopManager() {
    }

    public void queue(Loop<?> loop) {
        synchronized (lock) {
            loops.add(loop);
            lock.notifyAll();
        }
    }

    public int size() {
        return loops.size();
    }

    public Optional<Loop<?>> pollMostImportant() {
        synchronized (lock) {
            return Optional.ofNullable(loops.poll());
        }
    }

    public Optional<Loop<?>> pollMoreImportant(Loop<?> than) {
        synchronized (lock) {
            final Loop<?> peek = loops.peek();

            if (peek == null) {
                return Optional.empty();
            }

            if (peek.compareTo(than) > 0) {
                loops.remove();

                return Optional.of(peek);
            } else return Optional.empty();
        }
    }
}
