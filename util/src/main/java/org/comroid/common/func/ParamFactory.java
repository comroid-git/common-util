package org.comroid.common.func;

import org.jetbrains.annotations.Nullable;

public interface ParamFactory<P, T> extends Factory<T> {
    int counter();

    T create(@Nullable P parameter);

    @Override
    default T create() {
        return create(null);
    }

    abstract class Abstract<P, T> extends Factory.Abstract<T> implements ParamFactory<P, T> {
    }
}