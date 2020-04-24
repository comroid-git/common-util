package org.comroid.common.func;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.comroid.common.Polyfill;
import org.comroid.common.annotation.Blocking;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;

@FunctionalInterface
public interface Provider<T> extends Supplier<CompletableFuture<T>> {
    default boolean isInstant() {
        return this instanceof Now;
    }

    @Blocking
    default T now() {
        return get().join();
    }

    CompletableFuture<T> get();

    static <T> Provider<T> of(CompletableFuture<T> future) {
        return Polyfill.constantSupplier(future)::get;
    }

    static <T> Provider.Now<T> of(Supplier<T> supplier) {
        return supplier::get;
    }

    static <T> Provider.Now<T> constant(T value) {
        return Objects.isNull(value) ? empty() : (Now<T>) Support.Constant.cache.computeIfAbsent(value, Support.Constant::new);
    }

    static <T> Provider.Now<T> empty() {
        return (Now<T>) Support.EMPTY;
    }

    static <T> Provider<T> completingOnce(CompletableFuture<T> from) {
        return new Support.Once<>(from);
    }

    @Internal
    final class Support {
        private static final class Constant<T> implements Provider.Now<T> {
            private Constant(T value) {
                this.value = value;
            }

            @Override
            public T now() {
                return value;
            }
            private static final Map<Object, Constant<Object>> cache = new ConcurrentHashMap<>();
            private final T value;
        }

        private static final class Once<T> implements Provider<T> {
            public Once(CompletableFuture<T> from) {
                this.future = from;
            }

            @Override
            public CompletableFuture<T> get() {
                return future;
            }
            private final CompletableFuture<T> future;
        }
        private static final Provider<?> EMPTY = constant(null);
    }

    @FunctionalInterface
    interface Now<T> extends Provider<T> {
        @Override
        default boolean isInstant() {
            return true;
        }

        @Override
        T now();

        @Override
        @Contract("-> new")
        default CompletableFuture<T> get() {
            return CompletableFuture.completedFuture(now());
        }
    }
}
