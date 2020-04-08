package org.comroid.common.func;

import java.util.concurrent.CompletableFuture;

public interface Builder<T> extends Provider<T> {
    T build();

    @Override
    default CompletableFuture<T> get() {
        return CompletableFuture.completedFuture(build());
    }
}
