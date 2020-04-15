package org.comroid.dreadpool.loop;

import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;

import org.comroid.dreadpool.model.Loop;

public abstract class WhileDo<T> extends Loop<T> {
    public static final class Func extends WhileDo<Integer> {
        private final BooleanSupplier predicate;
        private final Runnable        action;

        public Func(int priority, BooleanSupplier predicate, Runnable action) {
            super(priority, val -> val + 1);

            this.predicate = predicate;
            this.action    = action;
        }

        @Override
        protected boolean continueLoop() {
            return predicate.getAsBoolean();
        }

        @Override
        protected void execute(Integer each) {
            action.run();
        }
    }

    private final IntFunction<T> producer;

    public WhileDo(int priority, IntFunction<T> producer) {
        super(priority);

        this.producer = producer;
    }

    @Override
    protected abstract boolean continueLoop();

    @Override
    protected final T produce(int loop) {
        return producer.apply(loop);
    }

    @Override
    protected abstract void execute(T each);
}
