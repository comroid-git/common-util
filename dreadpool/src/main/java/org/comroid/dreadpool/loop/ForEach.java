package org.comroid.dreadpool.loop;

import java.util.Iterator;

public abstract class ForEach<T> extends Loop<T> {
    private final Iterator<T> iterator;

    public ForEach(Iterable<T> iterable) {
        super(priority);
        this.iterator = iterable.iterator();
    }

    @Override
    protected final T produce(int loop) {
        return iterator.next();
    }

    @Override
    protected final boolean canContinue() {
        return iterator.hasNext();
    }
}
