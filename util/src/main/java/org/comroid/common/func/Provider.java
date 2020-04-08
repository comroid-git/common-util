package org.comroid.common.func;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Provider<T> extends Supplier<CompletableFuture<T>> {
    CompletableFuture<T> get();

    static <T> Provider<T> of(Supplier<T> supplier) {
        return () -> CompletableFuture.completedFuture(supplier.get());
    }

    static <T> Provider<T> of(final T value) {
        return () -> CompletableFuture.completedFuture(value);
    }
}
