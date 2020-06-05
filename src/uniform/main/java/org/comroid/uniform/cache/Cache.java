package org.comroid.uniform.cache;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Provider;
import org.comroid.common.map.ReferenceMap;
import org.comroid.common.ref.OutdateableReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Cache<K, V> extends Iterable<Map.Entry<K, V>>, ReferenceMap.Settable<K, V, Cache.Reference<K, V>> {
    boolean large();

    int size();

    boolean containsKey(K key);

    boolean containsValue(V value);

    default Stream<Reference<K, V>> stream() {
        return stream(any -> true);
    }

    Stream<Reference<K, V>> stream(Predicate<K> filter);

    @NotNull Reference<K, V> getReference(K key, boolean createIfAbsent);

    default boolean canProvide() {
        return false;
    }

    default CompletableFuture<V> provide(K key) {
        return Polyfill.failedFuture(new UnsupportedOperationException("Cache can't provide!"));
    }

    default void forEach(BiConsumer<K, V> action) {
        forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    class Reference<K, V> implements org.comroid.common.ref.Reference.Settable<V> {
        public final AtomicReference<V> reference = new AtomicReference<>(null);
        private final OutdateableReference<CompletableFuture<V>> firstValueFuture = new OutdateableReference<>();
        private final Object lock = Polyfill.selfawareLock();
        private final K key;

        public @NotNull K getKey() {
            return key;
        }

        public Reference(K key) {
            this.key = key;
            this.firstValueFuture.update(new CompletableFuture<>());
        }

        public Reference(K key, V initValue) {
            this.key = key;
            this.firstValueFuture.outdate();
        }

        public static <K, V> Reference<K, V> create() {
            //noinspection unchecked
            return (Reference<K, V>) new Reference<>(null);
        }

        public static <K, V> Reference<K, V> constant(K key, V value) {
            return new Reference<K, V>(key, value) {
                @Nullable
                @Override
                public V set(V value) {
                    throw new UnsupportedOperationException("Reference is constant");
                }
            };
        }

        @Nullable
        @Override
        public V set(V value) {
            synchronized (lock) {
                if (!firstValueFuture.isOutdated()
                        && !firstValueFuture.isNull()
                        && !Objects.requireNonNull(firstValueFuture.get(), "AssertionFailure").isDone()) {
                    firstValueFuture.get().complete(value);
                }

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

        @Override
        public Provider<V> provider() {
            if (firstValueFuture.isOutdated()) {
                firstValueFuture.outdate();
                return Provider.of(firstValueFuture.get());
            }

            return Provider.of(this);
        }
    }
}
