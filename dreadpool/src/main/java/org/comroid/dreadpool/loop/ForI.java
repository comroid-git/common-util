package org.comroid.dreadpool.loop;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.comroid.dreadpool.model.Loop;

public abstract class ForI<V> extends Loop<V> {
    public static final class IntFunc extends ForI<Integer> {
        private final IntSupplier      initOp;
        private final IntPredicate     continueTester;
        private final IntUnaryOperator accumulator;
        private final IntConsumer      action;

        public IntFunc(
                int priority,
                IntSupplier initOp,
                IntPredicate continueTester,
                IntUnaryOperator accumulator,
                IntConsumer action
        ) {
            super(priority);

            this.initOp         = initOp;
            this.continueTester = continueTester;
            this.accumulator    = accumulator;
            this.action         = action;

            super.v = init();
        }

        @Override
        protected boolean canContinueWith(Integer value) {
            return continueTester.test(value);
        }

        @Override
        protected Integer accumulate(Integer value) {
            return accumulator.applyAsInt(value);
        }

        @Override
        protected Integer init() {
            return initOp.getAsInt();
        }

        @Override
        protected void execute(Integer each) {
            action.accept(each);
        }
    }

    public static final class Func<T> extends ForI<T> {
        private final Supplier<T>      initOp;
        private final Predicate<T>     continueTester;
        private final UnaryOperator<T> accumulator;
        private final Consumer<T>      action;

        public Func(
                int priority,
                Supplier<T> initOp,
                Predicate<T> continueTester,
                UnaryOperator<T> accumulator,
                Consumer<T> action
        ) {
            super(priority);

            this.initOp         = initOp;
            this.continueTester = continueTester;
            this.accumulator    = accumulator;
            this.action         = action;
        }

        @Override
        protected boolean canContinueWith(T value) {
            return continueTester.test(value);
        }

        @Override
        protected T accumulate(T value) {
            return accumulator.apply(value);
        }

        @Override
        protected T init() {
            return initOp.get();
        }

        @Override
        protected void execute(T each) {
            action.accept(each);
        }
    }

    private V v;

    protected ForI(int priority) {
        super(priority);
    }

    @Override
    public boolean oneCycle() {
        if (canContinueWith(v)) {
            final boolean cont = super.oneCycle();
            v = accumulate(v);
            return cont;
        } else return false;
    }

    protected abstract V init();

    protected abstract boolean canContinueWith(V value);

    protected abstract V accumulate(V value);

    @Override
    protected boolean canContinue() {
        return canContinueWith(v);
    }

    @Override
    protected V produce(int loop) {
        return v;
    }
}
