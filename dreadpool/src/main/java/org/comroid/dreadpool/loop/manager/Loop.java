package org.comroid.dreadpool.loop.manager;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

import org.comroid.common.ref.OutdateableReference;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

public abstract class Loop<L> implements Comparable<Loop<?>>, Runnable, AutoCloseable {
    public static final int LOW_PRIO    = 0;
    public static final int MEDIUM_PRIO = 100;
    public static final int HIGH_PRIO   = 200;

    public static final Comparator<Loop<?>> LOOP_COMPARATOR =
            Comparator.<Loop<?>>comparingInt(Loop::priority).reversed();

    public final  CompletableFuture<L>          result      = new CompletableFuture<>();
    protected     int                           counter     = 0;
    private final OutdateableReference<Boolean> canContinue = new OutdateableReference<>();
    private final int                           priority;
    private       boolean                       closed;

    protected Loop(int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(@NotNull Loop<?> other) {
        return LOOP_COMPARATOR.compare(this, other);
    }

    @Override
    public final void run() {
        while (canContinue()) {
            oneCycle();
        }

        close();
    }

    @Internal
    public final boolean canContinue() {
        if (isClosed()) {
            return false;
        }

        if (canContinue.isOutdated() && canContinue.update(continueLoop())) {
            return true;
        }

        if (!canContinue.get()) {
            close();
            return false;
        } else {
            return true;
        }
    }

    @Internal
    protected boolean oneCycle() {
        if (!canContinue()) {
            throw new UnsupportedOperationException("Loop is closed");
        }

        final L it = produce(nextInt());

        this.execute(it);

        return canContinue();
    }

    @Override
    @Internal
    public void close() {
        this.closed = true;

        if (!result.isDone()) {
            result.complete(null);
        }
    }

    public final boolean isClosed() {
        return closed;
    }

    @Internal
    protected abstract boolean continueLoop();

    @Internal
    protected abstract L produce(int loop);

    @Internal
    private int nextInt() {
        return counter++;
    }

    @Internal
    public final void execute(L each) {
        if (canContinue() && !executeLoop(each)) {
            close();
        } else {
            canContinue.outdate();
        }
    }

    /**
     * @param each The current loop variable.
     *
     * @return Whether or not the loop should be closed after this invocation.
     */
    @Internal
    protected abstract boolean executeLoop(L each);

    public int prevInt() {
        return counter;
    }

    public final int priority() {
        return priority;
    }

    @Internal
    protected int peekNextInt() {
        return counter + 1;
    }
}
