package org.comroid.dreadpool.loop;

import java.util.Iterator;
import java.util.function.Consumer;

import org.comroid.dreadpool.loop.manager.Loop;

public abstract class ForEach<T> extends Loop<T> {
    public ForEach(int priority, Iterable<T> iterable) {
        super(priority);

        this.iterator = iterable.iterator();
    }

    @Override
    protected final boolean continueLoop() {
        return iterator.hasNext();
    }

    @Override
    protected final T produce(int loop) {
        return iterator.next();
    }

    @Override
    protected abstract boolean executeLoop(T each);

    public static final class Func<T> extends ForEach<T> {
        public Func(int priority, Iterable<T> iterable, Consumer<T> action) {
            super(priority, iterable);

            this.action = action;
        }

        @Override
        protected boolean executeLoop(T each) {
            action.accept(each);

            return continueLoop();
        }
        private final Consumer<T> action;
    }
    private final Iterator<T> iterator;
}
