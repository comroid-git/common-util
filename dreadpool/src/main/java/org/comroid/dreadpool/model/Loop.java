package org.comroid.dreadpool.model;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public abstract class Loop<L> implements Comparable<Loop<?>>, Runnable, AutoCloseable {
    public static final int LOW_PRIO = 0;
    public static final int MEDIUM_PRIO = 100;
    public static final int HIGH_PRIO = 200;

    public static final Comparator<Loop<?>> LOOP_COMPARATOR = Comparator.<Loop<?>>comparingInt(Loop::priority).reversed();

    public final CompletableFuture<L> result = new CompletableFuture<>();
    private final int priority;
    protected int counter = 0;
    private boolean closed;

    protected Loop(int priority) {
        this.priority = priority;
    }

    @Internal
    protected boolean oneCycle() throws UnsupportedOperationException {
        if (!canContinue()) return false;

        final L it = produce(nextInt());

        execute(it);

        return canContinue();
    }

    @Internal
    protected abstract boolean continueLoop();

    @Internal
    protected abstract L produce(int loop);

    @Internal
    protected abstract void execute(L each);

    @Internal
    private int nextInt() {
        return counter++;
    }

    @Override
    public int compareTo(@NotNull Loop<?> other) {
        return LOOP_COMPARATOR.compare(this, other);
    }

    public final boolean canContinue() throws UnsupportedOperationException {
        if (isClosed())
            throw new UnsupportedOperationException("Loop is closed!");

        return continueLoop();
    }

    public int prevInt() {
        return counter;
    }

    public final int priority() {
        return priority;
    }

    @Override
    public final void run() throws UnsupportedOperationException {
        while (canContinue()) oneCycle();

        close();
    }

    public final boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        this.closed = true;
    }
}
