package org.comroid.dreadpool.loop;

import org.comroid.dreadpool.model.Loop;

public abstract class Infinite<T> extends Loop<T> {
    public static final class Func extends Infinite<Object> {
        private final Runnable action;

        public Func(int priority, Runnable action) {
            super(priority, null);

            this.action = action;
        }

        @Override
        protected void execute() {
            action.run();
        }
    }

    private final T constant;

    public Infinite(int priority, T constant) {
        super(priority);

        this.constant = constant;
    }

    @Override
    protected boolean canContinue() {
        return true;
    }

    @Override
    protected T produce(int loop) {
        return constant;
    }

    @Override
    protected void execute(T constant) {
        execute();
    }

    protected abstract void execute();
}
