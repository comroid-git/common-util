package org.comroid.dreadpool.model;

import java.util.PriorityQueue;
import java.util.Queue;

import org.comroid.dreadpool.loop.Loop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LoopWorker extends Worker {
    private final Queue<Loop<?>> loopQueue = new PriorityQueue<>();

    public LoopWorker(@Nullable ThreadGroup group, @NotNull String name) {
        super(group, name);
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            synchronized (loopQueue) {
                try {
                    while (loopQueue.isEmpty()) {
                        loopQueue.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                final Loop<?> poll = loopQueue.poll();
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
