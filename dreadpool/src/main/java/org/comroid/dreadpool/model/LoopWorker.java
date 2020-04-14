package org.comroid.dreadpool.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LoopWorker extends Worker {
    private final LoopManager manager;

    public LoopWorker(
            @NotNull LoopManager manager, @Nullable ThreadGroup group, @NotNull String name
    ) {
        super(group, name);

        this.manager = manager;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            synchronized (manager.lock) {
                try {
                    while (manager.size() == 0) {
                        manager.lock.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                final Loop<?> poll = manager.mostImportant();
            }
        }
    }

    public void addLoop(Loop<?> loop) {
        synchronized (loopQueue) {
            loopQueue.add(loop);
            loopQueue.notify();
        }
    }
}
