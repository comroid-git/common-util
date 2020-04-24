package org.comroid.dreadpool.loop.manager;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import org.comroid.dreadpool.Worker;

import com.google.common.flogger.FluentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LoopWorker extends Worker {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final        LoopManager  manager;
    private              Loop<?>      current;

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
                if (!current.canContinue()) {
                    current = null;
                } else {
                    current.oneCycle();
                }
            } else {
                synchronized (manager.lock) {
                    Optional<Loop<?>> mostImportant = manager.pollMostImportant();
                    try {
                        if (!mostImportant.isPresent()) {
                            while (!mostImportant.isPresent() || manager.size() == 0) {
                                manager.lock.wait();
                                mostImportant = manager.pollMostImportant();
                            }
                        }
                    } catch (InterruptedException e) {
                        logger.at(Level.FINE)
                                .log("{} stopping!", toString());
                    } finally {
                        mostImportant.ifPresent(this::swapCurrent);
                    }
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
}
