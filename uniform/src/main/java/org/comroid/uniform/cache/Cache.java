package org.comroid.uniform.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Provider;
import org.comroid.common.ref.OutdateableReference;
import org.comroid.common.ref.Reference.Settable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Cache<K, V> extends Iterable<Map.Entry<K, V>> {
    class Reference<K, V> implements Settable<V> {
        public static <K, V> Reference<K, V> empty() {
            return (Reference<K, V>) EMPTY;
        }
        private static final Reference<?, ?> EMPTY = new Reference<Object, Object>(null) {
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
        public final  AtomicReference<V>                         reference        = new AtomicReference<>(
                null);

        public @NotNull K getKey() {
            return key;
        }
        private final OutdateableReference<CompletableFuture<V>> firstValueFuture = new OutdateableReference<>();
        private final Object                                     lock             = Polyfill.selfawareLock();
        private final K                                          key;

        public Reference(K key) {
            this.key = key;
            this.firstValueFuture.update(new CompletableFuture<>());
        }

        public Reference(K key, V initValue) {
            this.key = key;
            this.firstValueFuture.outdate();
        }

        @Nullable
        @Override
        public V set(V value) {
            synchronized (lock) {
                if (!firstValueFuture.isOutdated() && !firstValueFuture.get()
                                                                       .isDone()) firstValueFuture.get()
                                                                                                  .complete(
                                                                                                          value);

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

    boolean large();

    int size();

    boolean containsKey(K key);

    boolean containsValue(V value);

    default Stream<Reference<K, V>> stream() {
        return stream(any -> true);
    }

    Stream<Reference<K, V>> stream(Predicate<K> filter);

    @Nullable
    default V get(K key) {
        return getReference(key, false).get();
    }

    @NotNull Reference<K, V> getReference(K key, boolean createIfAbsent);

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
}
