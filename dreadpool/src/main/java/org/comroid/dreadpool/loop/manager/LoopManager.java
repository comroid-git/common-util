package org.comroid.dreadpool.loop.manager;

import java.io.Closeable;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.comroid.common.Polyfill;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LoopManager implements Closeable {
    public static final ThreadGroup     THREAD_GROUP = new ThreadGroup("LoopManager");

    public static LoopManager start(int parallelism) {
        return start(parallelism, THREAD_GROUP);
    }

    public static LoopManager start(int parallelism, @Nullable ThreadGroup group) {
        final LoopManager manager = new LoopManager();

        manager.workers = Collections.unmodifiableSet(IntStream.range(1, parallelism + 1)
                                                               .mapToObj(iter -> new LoopWorker(
                                                                       manager,
                                                                       group,
                                                                       String.format("LoopWorker @" + " " + "%s#%4d",
                                                                                     manager.toString(),
                                                                                     iter
                                                                       )
                                                               ))
                                                               .collect(Collectors.toSet()));
        manager.workers.forEach(Thread::start);

        return manager;
    }

    @Override
    public String toString() {
        return String.format("LoopManager{lock=%s}", queue);
    }
    final               Object          lock         = Polyfill.selfawareLock();
    private final       Queue<Loop<?>>  queue        = new PriorityQueue<>();
    private             Set<LoopWorker> workers;

    private LoopManager() {
    }

    @Override
    public void close() {
        workers.forEach(loopWorker -> {
            try {
                loopWorker.stop();
            } catch (Exception ignored) {
            }
        });
    }

    public <T> CompletableFuture<T> queue(@NotNull Loop<T> loop) {
        synchronized (lock) {
            queue.add(Objects.requireNonNull(loop, "Loop is null"));
            lock.notifyAll();
        }

        return loop.result;
    }

    public int size() {
        return queue.size();
    }

    Optional<Loop<?>> pollMostImportant() {
        synchronized (lock) {
            return Optional.ofNullable(queue.poll());
        }
    }

    @Deprecated
    Optional<Loop<?>> pollMoreImportant(Loop<?> than) {
        synchronized (lock) {
            final Loop<?> peek = queue.peek();

            if (peek == null) {
                return Optional.empty();
            }

            if (peek.compareTo(than) > 0) {
                queue.remove();

                return Optional.of(peek);
            } else return Optional.empty();
        }
    }
}
