package org.comroid.mutatio.ref;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class FutureReference<T> extends Reference.Support.Base<T> {
    public final CompletableFuture<T> future;

    public FutureReference() {
        this(new CompletableFuture<>());
    }

    public FutureReference(CompletableFuture<T> future) {
        super(parent, false);

        this.future = future;
    }

    @Nullable
    @Override
    protected T doGet() {
        return future.join();
    }

    public T compute(Supplier<T> supplier) {
        if (!future.isDone())
            future.complete(supplier.get());
        return get();
    }
}
