package org.comroid.dreadpool.loop.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.IntStream;

public final class LoopManager {
    public static final ThreadGroup THREAD_GROUP = new ThreadGroup("LoopManager");
    final               Queue<Loop<?>> loops     = new PriorityBlockingQueue<>();

    private LoopManager() {
    }

    public static LoopManager start(int parallelism) {
        return start(parallelism, THREAD_GROUP);
    }

    public static LoopManager start(int parallelism, @Nullable ThreadGroup group) {
        final LoopManager manager = new LoopManager();

        IntStream.range(1, parallelism + 1).mapToObj(iter -> new LoopWorker(manager,
                                                                            group,
                                                                            String.format("LoopWorker @ %s#%4d",
                                                                                          manager.toString(),
                                                                                          iter
                                                                            )
        )).forEach(Thread::start);

        return manager;
    }

    public void queue(@NotNull Loop<?> loop) {
        synchronized (loops) {
            loops.add(Objects.requireNonNull(loop, "Loop is null"));
            loops.notifyAll();
        }
    }

    public int size() {
        return loops.size();
    }

    Optional<Loop<?>> pollMostImportant() {
        synchronized (loops) {
            return Optional.ofNullable(loops.poll());
        }
    }

    Optional<Loop<?>> pollMoreImportant(Loop<?> than) {
        synchronized (loops) {
            final Loop<?> peek = loops.peek();

            if (peek == null) {
                return Optional.empty();
            }

            if (peek.compareTo(than) > 0) {
                loops.remove();

                return Optional.of(peek);
            } else
                return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return String.format("LoopManager{lock=%s}", loops);
    }
}
