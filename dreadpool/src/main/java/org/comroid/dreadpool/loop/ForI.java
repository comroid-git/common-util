package org.comroid.dreadpool.loop;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class ForI<V> extends Loop<V> {
    private final Supplier<V> initOp;
    private final Predicate<V> continueTester;
    private final Function<V, V> accumulator;

    private V v;

    public ForI(Supplier<V> initOp, Predicate<V> continueTester, Function<V, V> accumulator) {
        super(priority);
        this.initOp         = initOp;
        this.continueTester = continueTester;
        this.accumulator    = accumulator;

        this.v = initOp.get();
    }

    @Override
    protected V produce(int loop) {
        return v;
    }

    @Override
    public boolean oneCycle() {
        if (continueTester.test(v)) {
            final boolean cont = super.oneCycle();
            v = accumulator.apply(v);
            return cont;
        } else return false;
    }

    @Override
    protected boolean canContinue() {
        return continueTester.test(v);
    }
}
