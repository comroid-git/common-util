package org.comroid.common.func;

import org.jetbrains.annotations.Nullable;

public interface Factory<T> extends ParamFactory<Object, T> {
    int counter();

    T create();

    @Override
    default T create(@Nullable Object possiblyIgnored) {
        return create();
    }

    abstract class Abstract<T> extends ParamFactory.Abstract<Object, T> implements Factory<T> {}
}
