package org.comroid.common.func;

import java.util.concurrent.CompletableFuture;

public interface Factory<T> extends Provider<T> {
    int counter();

    T create();

    @Override
    default CompletableFuture<T> get() {
        return CompletableFuture.completedFuture(create());
    }

    @Override
    default T now() {
        return create();
    }
}
