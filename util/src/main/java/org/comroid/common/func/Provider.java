package org.comroid.common.func;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.comroid.common.annotation.Blocking;
import org.jetbrains.annotations.Contract;

public interface Provider<T> extends Supplier<CompletableFuture<T>> {
    static <T> Provider<T> of(Supplier<T> supplier) {
        return () -> CompletableFuture.completedFuture(supplier.get());
    }

    static <T> Provider<T> of(final T value) {
        return () -> CompletableFuture.completedFuture(value);
    }

    CompletableFuture<T> get();

    default boolean isInstant() {
        return this instanceof Now;
    }

    @Blocking
    default T now() {
        return get().join();
    }

    interface Now<T> extends Provider<T> {
        @Override
        @Contract("-> new")
        default CompletableFuture<T> get() {
            return CompletableFuture.completedFuture(now());
        }

        @Override
        T now();
    }
}
