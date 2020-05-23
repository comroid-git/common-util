package org.comroid.dreadpool.loop;

import java.util.function.BooleanSupplier;

import org.comroid.dreadpool.loop.manager.Loop;

public abstract class DoWhile extends Loop<Integer> {
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
    protected abstract boolean executeLoop(Integer each);

    public static final class Func extends DoWhile {
        private final BooleanSupplier continueTester;
        private final Runnable        action;

        public Func(int priority, BooleanSupplier continueTester, Runnable action) {
            super(priority, action);

            this.continueTester = continueTester;
            this.action         = action;
        }

        @Override
        protected boolean continueLoop() {
            return continueTester.getAsBoolean();
        }

        @Override
        protected boolean executeLoop(Integer each) {
            action.run();

            return true;
        }
    }
}
