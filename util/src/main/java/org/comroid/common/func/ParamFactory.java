package org.comroid.common.func;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

public interface ParamFactory<P, T> extends Provider.Now<T> {
    class Abstract<P, T> implements ParamFactory<P, T> {
        protected     int            counter = 0;
        private final Function<P, T> factory;

        public Abstract(Function<P, T> factory) {
            this.factory = factory;
        }

        protected Abstract() {
            this.factory = null;
        }

        @Override
        public T create(@Nullable P parameter) {
            if (factory == null) {
                throw new AbstractMethodError();
            }

            return factory.apply(parameter);
        }

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