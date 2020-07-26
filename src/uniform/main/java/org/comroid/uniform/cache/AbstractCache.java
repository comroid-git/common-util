package org.comroid.uniform.cache;

import org.comroid.api.Polyfill;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.mutatio.ref.ReferenceMap;
import org.comroid.trie.TrieMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractCache<K, V> implements Cache<K, V> {
    private final ReferenceMap<K, V, CacheReference<K, V>> cache;

    protected AbstractCache() {
        this(ReferenceMap.create());
    }

    protected AbstractCache(ReferenceMap<K, V, CacheReference<K, V>> cache) {
        this.cache = cache;
    }

    protected abstract CacheReference<K, V> advanceIntoCacheRef(Reference<V> reference);

    @NotNull
    @Override
    public Iterator<CacheReference<K, V>> iterator() {
        return a;
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
        return cache.size();
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
        return cache.stream(filter);
    }

    @Override
    public Pipe<?, CacheReference<K, V>> pipe(Predicate<K> filter) {
        return cache.stream(filter);
    }
}
