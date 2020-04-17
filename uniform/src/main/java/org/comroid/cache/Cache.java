package org.comroid.cache;

import org.comroid.common.Polyfill;
import org.comroid.common.ref.Reference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public interface Cache<K, V> extends Iterable<Map.Entry<K, V>> {
    boolean containsKey(K key);

    boolean containsValue(V value);

    @NotNull
    Reference<V> getReference(K key);

    @Nullable
    default V get(K key) {
        return getReference(key).get();
    }

    default Optional<V> wrap(K key) {
        return getReference(key).wrap();
    }

    default @NotNull V requireNonNull(K key) {
        return getReference(key).requireNonNull();
    }

    default @NotNull V requireNonNull(K key, String message) {
        return getReference(key).requireNonNull(message);
    }

    default boolean canProvide() {
        return false;
    }

    default CompletableFuture<V> provide(K key) {
        return Polyfill.failedFuture(new UnsupportedOperationException("Cache can't provide!"));
    }

    class Reference<V> implements org.comroid.common.ref.Reference<V> {
        private final Lock lock = new ReentrantLock(true);
        private @Nullable V value;

        @Nullable
        @Override
        public V get() {
            return null;
        }
    }
}
