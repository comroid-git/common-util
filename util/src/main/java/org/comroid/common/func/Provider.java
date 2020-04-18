package org.comroid.common.func;

import org.comroid.common.Polyfill;
import org.comroid.common.annotation.Blocking;
import org.jetbrains.annotations.Contract;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@FunctionalInterface
public interface Provider<T> extends Supplier<CompletableFuture<T>> {
    static <T> Provider<T> of(CompletableFuture<T> future) {
        return Polyfill.constantSupplier(future)::get;
    }

    static <T> Provider.Now<T> of(Supplier<T> supplier) {
        return supplier::get;
    }

    static <T> Provider.Now<T> constant(T value) {
        return Objects.isNull(value) ? empty() : (Now<T>) Support.Constant.cache.computeIfAbsent(value,
                Support.Constant::new
        );
    }

    static <T> Provider.Now<T> empty() {
        return (Now<T>) Support.EMPTY;
    }

    CompletableFuture<T> get();

    default boolean isInstant() {
        return this instanceof Now;
    }

    @Blocking
    default T now() {
        return get().join();
    }

    @FunctionalInterface
    interface Now<T> extends Provider<T> {
        @Override
        @Contract("-> new")
        default CompletableFuture<T> get() {
            return CompletableFuture.completedFuture(now());
        }

        @Override
        T now();

        @Override
        default boolean isInstant() {
            return true;
        }
    }

    final class Support {
        private static final Provider<?> EMPTY = constant(null);

        private static final class Constant<T> implements Provider.Now<T> {
            private static final Map<Object, Constant<Object>> cache = new ConcurrentHashMap<>();

            private final T value;

            private Constant(T value) {
                this.value = value;
            }

            @Override
            public T now() {
                return value;
            }
        }
    }
}
