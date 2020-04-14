package org.comroid.dreadpool.model;

import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

public final class LoopManager {
    final         Object        lock  = new Object() {
        @SuppressWarnings("FieldMayBeFinal")
        private volatile Object selfaware_keepalive = LoopManager.this.lock;
    };
    private final Queue<Loop<?>> loops = new PriorityQueue<>();

    public void queue(Loop<?> loop) {
        synchronized (lock) {
            loops.add(loop);
            lock.notifyAll();
        }
    }

    public int size() {
        return loops.size();
    }

    public synchronized Optional<Loop<?>> mostImportant() {
        return Optional.ofNullable(loops.poll());
    }
}
