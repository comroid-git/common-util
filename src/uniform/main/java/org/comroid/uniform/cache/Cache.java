package org.comroid.uniform.cache;

import org.comroid.api.Polyfill;
import org.comroid.api.Provider;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.ref.KeyedReference;
import org.comroid.mutatio.ref.ReferenceMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public interface Cache<K, V> extends Iterable<CacheReference<K, V>>, ReferenceMap<K, V, CacheReference<K, V>> {
    boolean large();

    <R> Processor<R> accessor(K key, String name, Processor.Advancer<V, ? extends R> advancer);

    @Override
    default Pipe<?, V> pipe() {
        return pipe(any -> true).map(org.comroid.mutatio.ref.Reference::get);
    }

    default boolean canProvide() {
        return false;
    }

    default CompletableFuture<V> provide(K key) {
        return Polyfill.failedFuture(new UnsupportedOperationException("Cache can't provide!"));
    }

    @Override
    default void forEach(BiConsumer<K, V> action) {
        forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    class Reference<K, V> extends KeyedReference.Basic<K, V> {
        public final AtomicReference<V> reference = new AtomicReference<>(null);
        private final org.comroid.mutatio.ref.Reference<CompletableFuture<V>> firstValueFuture
                = org.comroid.mutatio.ref.Reference.create();
        private final Object lock = Polyfill.selfawareLock();

        public Reference(K key) {
            super(true, key, null);

            this.firstValueFuture.update(new CompletableFuture<>());
        }

        public Reference(K key, V initValue) {
            super(true, key, initValue);

            this.firstValueFuture.outdate();
        }

        public static <K, V> Reference<K, V> create() {
            //noinspection unchecked
            return (Reference<K, V>) new Reference<>(null);
        }

        public static <K, V> Reference<K, V> constant(K key, V value) {
            return new Reference<K, V>(key, value) {
                @Override
                public boolean isMutable() {
                    return false;
                }
            };
        }

        @Override
        protected boolean doSet(V value) {
            synchronized (lock) {
                if (!firstValueFuture.isOutdated()
                        && !firstValueFuture.isNull()
                        && !Objects.requireNonNull(firstValueFuture.get(), "AssertionFailure").isDone()) {
                    firstValueFuture.requireNonNull().complete(value);
                }

                return reference.getAndSet(value) != value;
            }
        }

        @Nullable
        @Override
        protected V doGet() {
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
