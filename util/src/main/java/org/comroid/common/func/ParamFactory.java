package org.comroid.common.func;

import org.jetbrains.annotations.Nullable;

public interface ParamFactory<P, T> extends Provider.Now<T> {
    abstract class Abstract<P, T> implements ParamFactory<P, T> {
        protected int counter;

        @Override
        public final int counter() {
            return counter++;
        }

        @Override
        public final int peekCounter() {
            return counter;
        }
    }

    @Override
    default T now() {
        return create();
    }

    default T create() {
        return create(null);
    }

    T create(@Nullable P parameter);

    int counter();

    int peekCounter();
}