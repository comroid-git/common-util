package org.comroid.common.func;

public interface Factory<T> {
    int counter();

    T create();
}
