package org.comroid.uniform.cache;

import org.comroid.common.func.Provider;
import org.comroid.common.iter.Span;
import org.comroid.mutatio.pipe.Pipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BasicCache<K, V> implements Cache<K, V> {
    public static final int DEFAULT_LARGE_THRESHOLD = 250;
    private final @Nullable Provider.Now<V> emptyValueProvider;
    private final Map<K, Reference<K, V>> cache;
    private final int largeThreshold;

    public BasicCache() {
        this(DEFAULT_LARGE_THRESHOLD);
    }

    public BasicCache(int largeThreshold) {
        this(largeThreshold, new ConcurrentHashMap<>());
    }

    protected BasicCache(Map<K, Reference<K, V>> map) {
        this(DEFAULT_LARGE_THRESHOLD, map);
    }

    protected BasicCache(int largeThreshold, Map<K, Reference<K, V>> map) {
        this(largeThreshold, map, null);
    }

    protected BasicCache(int largeThreshold,
                         Map<K, Reference<K, V>> map,
                         @Nullable Provider.Now<V> emptyValueProvider) {
        this.largeThreshold = largeThreshold;
        this.cache = map;
        this.emptyValueProvider = emptyValueProvider;
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
        return (large()
                        ? cache.entrySet().parallelStream()
                        : cache.entrySet().stream()
        ).filter(entry -> filter.test(entry.getKey()))
                .map(Map.Entry::getValue);
    }

    @Override
    public Pipe<?, Reference<K, V>> pipe(Predicate<K> filter) {
        return Pipe.of(cache.entrySet())
                .filter(entry -> filter.test(entry.getKey()))
                .map(Map.Entry::getValue);
    }

    @Override
    public @NotNull Reference<K, V> getReference(K key, boolean createIfAbsent) {
        return createIfAbsent
                ? cache.computeIfAbsent(key, Reference::new)
                : cache.getOrDefault(key,
                emptyValueProvider == null
                        ? Reference.create()
                        : Reference.constant(key, emptyValueProvider.now()));
    }
}
