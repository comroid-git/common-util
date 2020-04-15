package org.comroid.dreadpool.loop.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class LoopWorker extends Worker {
    private final LoopManager manager;
    private Loop<?> current;

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
            if (current != null) {
                final Loop<?> peek = peek();
                if (!peek.continueLoop()) {
                    current = null;
                } else peek.oneCycle();
            } else synchronized (manager.lock) {
                try {
                    while (manager.size() == 0) {
                        manager.lock.wait();
                    }

                    swapCurrent(manager.pollMostImportant()
                            .orElseThrow(() -> new AssertionError(
                                    "Could not retrieve most important loop")));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void swapCurrent(Loop<?> loop) {
        if (current != null) {
            manager.queue(current);
            current = null; // explicit overwriting
        }

        current = loop;
    }

    private Loop<?> peek() {
        manager.pollMoreImportant(current)
                .ifPresent(prio -> {
                    if (prio == current)
                        throw new AssertionError();

                    swapCurrent(prio);
                });

        return current;
    }
}
