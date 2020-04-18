package org.comroid.uniform.cache;

import org.comroid.common.Polyfill;
import org.comroid.common.ref.Reference.Settable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Cache<K, V> extends Iterable<Map.Entry<K, V>> {
    @NotNull Reference<K, V> getReference(K key, boolean createIfAbsent);

    boolean large();

    int size();

    boolean containsKey(K key);

    boolean containsValue(V value);

    Stream<Reference<K, V>> stream(Predicate<K> filter);

    default Stream<Reference<K, V>> stream() {
        return stream(any -> true);
    }

    @Nullable
    default V get(K key) {
        return getReference(key, false).get();
    }

    default Optional<V> wrap(K key) {
        return getReference(key, false).wrap();
    }

    default @NotNull V requireNonNull(K key) {
        return getReference(key, false).requireNonNull();
    }

    default @NotNull V requireNonNull(K key, String message) {
        return getReference(key, false).requireNonNull(message);
    }

    default boolean canProvide() {
        return false;
    }

    default CompletableFuture<V> provide(K key) {
        return Polyfill.failedFuture(new UnsupportedOperationException("Cache can't provide!"));
    }

    default @Nullable V set(K key, V newValue) {
        return getReference(key, false).set(newValue);
    }

    default void forEach(BiConsumer<K, V> action) {
        forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    class Reference<K, V> implements Settable<V> {
        private static final Reference<?, ?> EMPTY            = new Reference<Object, Object>(null) {
            @Nullable
            @Override
            public Object set(Object value) {
                throw new UnsupportedOperationException("Cannot overwrite Empty Reference!");
            }

            @Nullable
            @Override
            public Object get() {
                return null;
            }
        };

        public final AtomicReference<V> reference = new AtomicReference<>(null);
        private final Object             lock      = Polyfill.selfawareLock();
        private final K                  key;

        public Reference(K key) {
            this.key = key;
        }

        public @NotNull K getKey() {
            return key;
        }

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

        public static <K, V> Reference<K, V> empty() {
            return (Reference<K, V>) EMPTY;
        }
    }
}