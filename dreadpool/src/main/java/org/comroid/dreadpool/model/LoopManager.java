package org.comroid.dreadpool.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.IntStream;

public final class LoopManager {
    public static final ThreadGroup THREAD_GROUP = new ThreadGroup("LoopManager");
    final Object lock = new Object() {
        @SuppressWarnings("FieldMayBeFinal")
        private volatile Object selfaware_keepalive = LoopManager.this.lock;

        @Override
        public String toString() {
            return "SelfAware Lock";
        }
    };
    private final Queue<Loop<?>> loops = new PriorityQueue<>();

    private LoopManager() {
    }

    public static LoopManager start(int parallelism) {
        return start(parallelism, THREAD_GROUP);
    }

    public static LoopManager start(int parallelism, @Nullable ThreadGroup group) {
        final LoopManager manager = new LoopManager();

        IntStream.range(1, parallelism + 1)
                .mapToObj(iter -> new LoopWorker(
                        manager,
                        group,
                        String.format("LoopWorker @ %s#%4d", manager.toString(), iter)
                ))
                .forEach(Thread::start);

        return manager;
    }

    public void queue(@NotNull Loop<?> loop) {
        synchronized (lock) {
            loops.add(Objects.requireNonNull(loop, "Loop is null"));
            lock.notifyAll();
        }
    }

    public int size() {
        return loops.size();
    }

    Optional<Loop<?>> pollMostImportant() {
        synchronized (lock) {
            return Optional.ofNullable(loops.poll());
        }
    }

    Optional<Loop<?>> pollMoreImportant(Loop<?> than) {
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

    @Override
    public String toString() {
        return String.format("LoopManager{lock=%s}", lock);
    }
}
