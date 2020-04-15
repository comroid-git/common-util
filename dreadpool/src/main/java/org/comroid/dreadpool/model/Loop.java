package org.comroid.dreadpool.model;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public abstract class Loop<L> implements Comparable<Loop<?>>, Runnable {
    public static final int LOW_PRIO = 0;
    public static final int MEDIUM_PRIO = 100;
    public static final int HIGH_PRIO = 200;

    public static final Comparator<Loop<?>> LOOP_COMPARATOR = Comparator.<Loop<?>>comparingInt(Loop::priority).reversed();

    public final CompletableFuture<L> result = new CompletableFuture<>();
    private final int priority;
    protected int counter = 0;

    protected Loop(int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(@NotNull Loop<?> other) {
        return LOOP_COMPARATOR.compare(this, other);
    }

    public boolean oneCycle() {
        if (!canContinue()) return false;

        final L it = produce(nextInt());

        execute(it);

        return canContinue();
    }

    protected abstract boolean canContinue();

    protected abstract L produce(int loop);

    private int nextInt() {
        return counter++;
    }

    protected abstract void execute(L each);

    public int lastInt() {
        return counter;
    }

    public final int priority() {
        return priority;
    }

    @Override
    public final void run() {
        while (canContinue()) oneCycle();
    }
}
