package org.comroid.uniform.cache;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.comroid.common.iter.Span;

import org.jetbrains.annotations.NotNull;

public class BasicCache<K, V> implements Cache<K, V> {
    public static final int DEFAULT_LARGE_THRESHOLD = 250;

    public BasicCache() {
        this(DEFAULT_LARGE_THRESHOLD);
    }
    public BasicCache(int largeThreshold) {
        this.largeThreshold = largeThreshold;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return stream().map(ref -> new AbstractMap.SimpleEntry<K, V>(ref.getKey(), ref.get()) {
            @Override
            public V setValue(V value) {
                return getReference(getKey(), false).set(value);
            }
        })
                .map(it -> (Map.Entry<K, V>) it)
                .collect(Span.collector())
                .iterator();
    }
    private final Map<K, Reference<K, V>> cache = new ConcurrentHashMap<>();
    private final int                     largeThreshold;

    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return stream().anyMatch(ref -> ref.process()
                .test(value::equals));
    }

    @Override
    public boolean large() {
        return size() < largeThreshold;
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public final Stream<Reference<K, V>> stream(Predicate<K> filter) {
        return (
                large()
                        ? cache.entrySet()
                        .parallelStream()
                        : cache.entrySet()
                                .stream()
        ).filter(entry -> filter.test(entry.getKey()))
                .map(Map.Entry::getValue);
    }

    @Override
    public @NotNull Reference<K, V> getReference(K key, boolean createIfAbsent) {
        return createIfAbsent ? cache.computeIfAbsent(key, Reference::new) : cache.getOrDefault(key, Cache.Reference.empty());
    }
}
