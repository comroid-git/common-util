package org.comroid.uniform.cache;

import org.comroid.api.Polyfill;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.ref.ReferenceMap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface Cache<K, V> extends Iterable<Map.Entry<K, V>>, ReferenceMap<K, V, CacheReference<K, V>> {
    boolean large();

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
}
