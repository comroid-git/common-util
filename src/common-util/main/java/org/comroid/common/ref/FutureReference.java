package org.comroid.common.ref;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public final class FutureReference<T> implements Reference<T> {
    public final CompletableFuture<T> future;

    public FutureReference() {
        this(new CompletableFuture<>());
    }

    public FutureReference(CompletableFuture<T> future) {
        this.future = future;
    }

    @Nullable
    @Override
    public T get() {
        return future.join();
    }
}
