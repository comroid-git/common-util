package org.comroid.uniform.cache;

import org.comroid.api.Provider;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.mutatio.span.Span;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicCache<K, V> extends AbstractCache<K, V> {
    public static final int DEFAULT_LARGE_THRESHOLD = 250;
    private final @Nullable Provider.Now<V> emptyValueProvider;
    private final Map<K, CacheReference<K, V>> cache;
    private final int largeThreshold;

    public BasicCache() {
        this(DEFAULT_LARGE_THRESHOLD);
    }

    public BasicCache(int largeThreshold) {
        this(largeThreshold, new ConcurrentHashMap<>());
    }

    protected BasicCache(Map<K, CacheReference<K, V>> map) {
        this(DEFAULT_LARGE_THRESHOLD, map);
    }

    protected BasicCache(int largeThreshold, Map<K, CacheReference<K, V>> map) {
        this(largeThreshold, map, null);
    }

    protected BasicCache(int largeThreshold,
                         Map<K, CacheReference<K, V>> map,
                         @Nullable Provider.Now<V> emptyValueProvider) {
        super(cache);
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
        return stream(any -> true)
                .<Map.Entry<K, V>>map(ref -> new AbstractMap.SimpleEntry<K, V>(ref.getKey(), ref.get()) {
                    @Override
                    public V setValue(V value) {
                        final CacheReference<K, V> reference = getReference(getKey(), false);
                        final V prev = reference.get();
                        reference.set(value);
                        return prev;
                    }
                })
                .collect(Span.collector())
                .iterator();
    }

    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return stream().anyMatch(value::equals);
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
    public final Stream<CacheReference<K, V>> stream(Predicate<K> filter) {
        return (large()
                ? cache.entrySet().parallelStream()
                : cache.entrySet().stream()
        ).filter(entry -> filter.test(entry.getKey()))
                .map(Map.Entry::getValue);
    }

    @Override
    public Pipe<?, CacheReference<K, V>> pipe(Predicate<K> filter) {
        return Pipe.of(cache.entrySet())
                .filter(entry -> filter.test(entry.getKey()))
                .map(Map.Entry::getValue);
    }

    @Override
    public @NotNull CacheReference<K, V> getReference(K key, boolean createIfAbsent) {
        return createIfAbsent
                ? cache.computeIfAbsent(key, CacheReference::new)
                : cache.getOrDefault(key,
                emptyValueProvider == null
                        ? CacheReference.create()
                        : CacheReference.constant(key, emptyValueProvider.now()));
    }

    @Override
    public ReferenceIndex<Map.Entry<K, V>> entryIndex() {
        return new EntryIndex();
    }

    private final class EntryIndex implements ReferenceIndex<Map.Entry<K, V>> {
        private final BasicCache<K, V> it = BasicCache.this;

        @Override
        public List<Map.Entry<K, V>> unwrap() {
            return BasicCache.this.cache.entrySet()
                    .stream()
                    .map(entry -> new AbstractMap.SimpleImmutableEntry<>(
                            entry.getKey(),
                            entry.getValue().get()
                    ))
                    .collect(Collectors.toList());
        }

        @Override
        public int size() {
            return BasicCache.this.size();
        }

        @Override
        public boolean add(Map.Entry<K, V> entry) {
            return set(entry.getKey(), entry.getValue());
        }

        @Override
        public boolean remove(Map.Entry<K, V> entry) {
            if (!containsKey(entry.getKey()))
                return false;

            return BasicCache.this
                    .getReference(entry.getKey(), false)
                    .set(null);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Clear operation not supported by Cache");
        }

        @Override
        public org.comroid.mutatio.ref.Reference<Map.Entry<K, V>> getReference(int index) {
            return org.comroid.mutatio.ref.Reference.conditional(
                    () -> BasicCache.this.size() < index,
                    () -> unwrap().get(index)
            );
        }
    }
}
