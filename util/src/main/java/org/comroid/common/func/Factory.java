package org.comroid.common.func;

public interface Factory<T> extends Provider.Now<T> {
    int counter();

    T create();

    @Override
    default T now() {
        return create();
    }

    abstract class Abstract<T> implements Factory<T> {
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
