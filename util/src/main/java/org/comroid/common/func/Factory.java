package org.comroid.common.func;

import org.jetbrains.annotations.Nullable;

public interface Factory<T> extends ParamFactory<Object, T> {
    abstract class Abstract<T> extends ParamFactory.Abstract<Object, T> implements Factory<T> {}

    int counter();

    @Override
    default T create(@Nullable Object possiblyIgnored) {
        return create();
    }

    T create();
}
