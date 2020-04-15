package org.comroid.dreadpool.loop;

import java.util.Iterator;
import java.util.function.Consumer;

import org.comroid.dreadpool.loop.manager.Loop;

public abstract class ForEach<T> extends Loop<T> {
    public static final class Func<T> extends ForEach<T> {
        private final Consumer<T> action;

        public Func(int priority, Iterable<T> iterable, Consumer<T> action) {
            super(priority, iterable);

            this.action = action;
        }

        @Override
        protected void executeLoop(T each) {
            action.accept(each);
        }
    }

    private final Iterator<T> iterator;

    public ForEach(int priority, Iterable<T> iterable) {
        super(priority);

        this.iterator = iterable.iterator();
    }

    @Override
    protected abstract void executeLoop(T each);

    @Override
    protected final T produce(int loop) {
        return iterator.next();
    }

    @Override
    protected final boolean continueLoop() {
        return iterator.hasNext();
    }
}
