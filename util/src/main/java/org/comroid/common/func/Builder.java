package org.comroid.common.func;

public interface Builder<T> extends Provider.Now<T> {
    @Override
    default T now() {
        return build();
    }

    T build();
}
