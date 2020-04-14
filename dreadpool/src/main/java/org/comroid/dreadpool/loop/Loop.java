package org.comroid.dreadpool.loop;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

public abstract class Loop<L> implements Comparable<Loop<?>> {
    private static final Comparator<Loop<?>> LoopComparator = Comparator.<Loop<?>>comparingInt(Loop::priority).reversed();
    private final        int                 priority;
    private              int                 counter        = 0;

    protected Loop(int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(@NotNull Loop<?> other) {
        return LoopComparator.compare(this, other);
    }

    public boolean oneCycle() {
        if (!canContinue()) return false;

        final L it = produce(nextInt());

        execute(it);

        return canContinue();
    }

    protected abstract boolean canContinue();

    protected abstract L produce(int loop);

    public final int nextInt() {
        return counter++;
    }

    protected abstract void execute(L each);

    public final int priority() {
        return priority;
    }
}
