package org.comroid.common.func;

public interface ThrowingSupplier<T, E extends Throwable> {
    T get() throws E;
}
