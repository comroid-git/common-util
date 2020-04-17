package org.comroid.uniform.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProvidedCache<K, V> implements Cache<K, V> {
    private final ConcurrentHashMap<K, Cache.Reference<V>> cache = new ConcurrentHashMap<>();

    @Override
    public boolean containsKey(K key) {
        return false;
    }

    @Override
    public boolean containsValue(V value) {
        return false;
    }

    @Nullable
    @Override
    public V get(K key) {
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
