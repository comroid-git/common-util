package org.comroid.uniform.cache;

import org.comroid.common.Polyfill;
import org.comroid.common.ref.Reference.Settable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Ref;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Cache<K, V> extends Iterable<Map.Entry<K, V>> {
    @NotNull Reference<V> getReference(K key);

    boolean large();

    int size();

    boolean containsKey(K key);

    boolean containsValue(V value);

    Stream<Reference<V>> stream(Predicate<K> filter);

    default Stream<Reference<V>> stream() {
        return stream(any -> true);
    }

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

    default @Nullable V set(K key, V newValue) {
        return getReference(key).set(newValue);
    }

    class Reference<V> implements Settable<V> {
        public final  AtomicReference<V> reference = new AtomicReference<>(null);
        private final Object             lock      = Polyfill.selfawareLock();

        @Nullable
        @Override
        public V set(V value) {
            synchronized (lock) {
                return reference.getAndSet(value);
            }
        }

        @Nullable
        @Override
        public V get() {
            synchronized (lock) {
                return reference.get();
            }
        }
    }
}
