package org.comroid.dreadpool.loop;

import org.comroid.dreadpool.loop.manager.Loop;

public abstract class Infinite<T> extends Loop<T> {
    public Infinite(int priority, T constant) {
        super(priority);

        this.constant = constant;
    }

    @Override
    protected boolean continueLoop() {
        return true;
    }

    @Override
    protected T produce(int loop) {
        return constant;
    }

    @Override
    protected boolean executeLoop(T constant) {
        execute();

        return true;
    }

    protected abstract void execute();

    public static final class Func extends Infinite<Object> {
        public Func(int priority, Runnable action) {
            super(priority, null);

            this.action = action;
        }

        @Override
        protected void execute() {
            action.run();
        }
        private final Runnable action;
    }
    private final T constant;
}
