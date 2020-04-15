package org.comroid.dreadpool.loop.manager;

import org.comroid.common.ref.OutdateableReference;
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
    private final OutdateableReference<Boolean> canContinue = new OutdateableReference<>();
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

        this.execute(it);

        return canContinue();
    }

    @Internal
    protected abstract boolean continueLoop();

    @Internal
    protected abstract void executeLoop(L each);

    @Internal
    protected abstract L produce(int loop);

    @Internal
    private int nextInt() {
        return counter++;
    }

    @Override
    public int compareTo(@NotNull Loop<?> other) {
        return LOOP_COMPARATOR.compare(this, other);
    }

    @Internal
    public final void execute(L each) {
        // itsy bitsy boolean operations to check whether we should continue
        if (canContinue.outdate() || canContinue()) {
            executeLoop(each);
        }
    }

    @Internal
    public final boolean canContinue() throws UnsupportedOperationException {
        if (isClosed())
            throw new UnsupportedOperationException("Loop is closed!");

        if (canContinue.isOutdated())
            if (canContinue.update(continueLoop()))
                return true;
            else if (!result.isDone()) result.complete(null);
        return canContinue.get();
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
