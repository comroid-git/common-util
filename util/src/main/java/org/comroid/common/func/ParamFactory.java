package org.comroid.common.func;

import org.jetbrains.annotations.Nullable;

public interface ParamFactory<P, T> extends Provider.Now<T> {
    int counter();

    T create(@Nullable P parameter);

    default T create() {
        return create(null);
    }

    @Override
    default T now() {
        return create();
    }

    abstract class Abstract<P, T> implements ParamFactory<P, T> {
        protected int counter;

        @Override
        public final int counter() {
            return counter++;
        }

        protected final int peekCounter() {
            return counter;
        }
    }
}