package org.comroid.uniform.cache;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ProvidedCache<K, V> implements Cache<K, V> {
    private final ConcurrentHashMap<K, Cache.Reference<V>> cache = new ConcurrentHashMap<>();

    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return stream() // todo All of this
    }

    @Override
    public boolean large() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public final Stream<Reference<V>> stream(Predicate<K> filter) {
        return (large()
                ? cache.entrySet()
                .parallelStream()
                : cache.entrySet()
                        .stream()
        ).filter(entry -> filter.test(entry.getKey()))
                .map(Map.Entry::getValue);
    }

    @Override
    public @NotNull Reference<V> getReference(K key) {
        return null;
    }

    @Override
    public boolean canProvide() {
        return false;
    }

    @Override
    public CompletableFuture<V> provide(K key) {
        return null;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return null;
    }
}
