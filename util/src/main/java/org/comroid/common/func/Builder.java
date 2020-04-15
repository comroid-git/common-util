package org.comroid.common.func;

public interface Builder<T> extends Provider.Now<T> {
    T build();

    @Override
    default T now() {
        return build();
    }
}
