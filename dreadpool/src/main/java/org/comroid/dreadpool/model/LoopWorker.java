package org.comroid.dreadpool.model;

import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LoopWorker extends Worker {
    private final LoopManager    manager;
    private final Queue<Loop<?>> stack = new PriorityQueue<>();

    public LoopWorker(
            @NotNull LoopManager manager, @Nullable ThreadGroup group, @NotNull String name
    ) {
        super(group, name);

        this.manager = Objects.requireNonNull(manager, "LoopManager cannot be null");
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            if (!stack.isEmpty()) {
                final Loop<?> peek = peek();
                if (!peek.canContinue()) {
                    stack.remove();
                } else peek.oneCycle();
            } else synchronized (manager.lock) {
                try {
                    while (manager.size() == 0) {
                        manager.lock.wait();
                    }

                    stack.add(manager.pollMostImportant()
                                     .orElseThrow(() -> new AssertionError(
                                             "Could not retrieve most important loop")));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Loop<?> peek() {
        manager.pollMoreImportant(stack.peek())
               .ifPresent(stack::add);

        return stack.peek();
    }
}
