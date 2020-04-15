package org.comroid.dreadpool.loop;

import java.util.function.BooleanSupplier;

import org.comroid.dreadpool.model.Loop;

public abstract class DoWhile extends Loop<Integer> {
    public static final class Func extends DoWhile {
        private final BooleanSupplier continueTester;
        private final Runnable        action;

        @Override
        protected boolean continueLoop() {
            return continueTester.getAsBoolean();
        }

        @Override
        protected void executeLoop(Integer each) {
            action.run();
        }

        public Func(int priority, BooleanSupplier continueTester, Runnable action) {
            super(priority, action);

            this.continueTester = continueTester;
            this.action         = action;
        }
    }

    public DoWhile(int priority) {
        super(priority);

        this.executeLoop(-1);
    }

    private DoWhile(int priority, Runnable initCall) {
        super(priority);

        initCall.run();
    }

    @Override
    protected abstract boolean continueLoop();

    @Override
    protected Integer produce(int loop) {
        return loop + 1;
    }

    @Override
    protected abstract void executeLoop(Integer each);
}
