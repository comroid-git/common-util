package org.comroid.uniform.cache;

import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractCache<K, V> implements Cache<K, V> {
    private final Reference<Boolean> isLarge;

    @Override
    public boolean large() {
        return isLarge.requireNonNull();
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return null;
    }

    @Override
    public CacheReference<K, V> getReference(K key, boolean createIfAbsent) {
        return null;
    }

    @Override
    public ReferenceIndex<Map.Entry<K, V>> entryIndex() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean containsKey(K key) {
        return false;
    }

    @Override
    public boolean containsValue(V value) {
        return false;
    }

    @Override
    public Stream<CacheReference<K, V>> stream(Predicate<K> filter) {
        return null;
    }

    @Override
    public Pipe<?, CacheReference<K, V>> pipe(Predicate<K> filter) {
        return null;
    }
}
