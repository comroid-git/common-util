package org.comroid.dreadpool.loop.manager;

import org.comroid.common.ref.OutdateableReference;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public abstract class Loop<L> implements Comparable<Loop<?>>, Runnable, AutoCloseable {
    public static final int LOW_PRIO    = 0;
    public static final int MEDIUM_PRIO = 100;
    public static final int HIGH_PRIO   = 200;

    public static final Comparator<Loop<?>> LOOP_COMPARATOR
            = Comparator.<Loop<?>>comparingInt(Loop::priority).reversed();

    public final  CompletableFuture<L>          result      = new CompletableFuture<>();
    private final OutdateableReference<Boolean> canContinue = new OutdateableReference<>();
    private final int                           priority;
    protected     int                           counter     = 0;
    private       boolean                       closed;

    protected Loop(int priority) {
        this.priority = priority;
    }

    @Internal
    protected boolean oneCycle() {
        if (!canContinue())
            throw new UnsupportedOperationException("Loop is closed");

        final L it = produce(nextInt());

        this.execute(it);

        return canContinue();
    }

    @Internal
    protected abstract boolean continueLoop();

    /**
     * @param each The current loop variable.
     * @return Whether or not the loop should be closed after this invocation.
     */
    @Internal
    protected abstract boolean executeLoop(L each);

    @Internal
    protected abstract L produce(int loop);

    @Internal
    private int nextInt() {
        return counter++;
    }

    @Internal
    protected int peekNextInt() {
        return counter + 1;
    }

    @Override
    public int compareTo(@NotNull Loop<?> other) {
        return LOOP_COMPARATOR.compare(this, other);
    }

    @Internal
    public final void execute(L each) {
        if (canContinue() && !executeLoop(each))
            close();
        else canContinue.outdate();
    }

    @Internal
    public final boolean canContinue() {
        if (isClosed()) return false;

        if (canContinue.isOutdated() && canContinue.update(continueLoop()))
            return true;

        if (!canContinue.get()) {
            close();
            return false;
        } else return true;
    }

    public int prevInt() {
        return counter;
    }

    public final int priority() {
        return priority;
    }

    @Override
    public final void run() {
        while (canContinue())
            oneCycle();

        close();
    }

    public final boolean isClosed() {
        return closed;
    }

    @Override
    @Internal
    public void close() {
        this.closed = true;

        if (!result.isDone())
            result.complete(null);
    }
}