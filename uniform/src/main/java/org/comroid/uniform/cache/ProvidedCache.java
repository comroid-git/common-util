package org.comroid.uniform.cache;

import org.comroid.common.iter.Span;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ProvidedCache<K, V> implements Cache<K, V> {
    public static final Executor DEFAULT_EXECUTOR        = ForkJoinPool.commonPool();
    public static final int      DEFAULT_LARGE_THRESHOLD = 250;

    private final Map<K, Cache.Reference<K, V>>     cache = new ConcurrentHashMap<>();
    private final Executor                          providerWriteExecutor;
    private final Function<K, CompletableFuture<V>> valueProvider;
    private final int                               largeThreshold;

    public ProvidedCache(Function<K, CompletableFuture<V>> valueProvider) {
        this(DEFAULT_EXECUTOR, valueProvider, DEFAULT_LARGE_THRESHOLD);
    }

    public ProvidedCache(Function<K, CompletableFuture<V>> valueProvider, int largeThreshold) {
        this(DEFAULT_EXECUTOR, valueProvider, largeThreshold);
    }

    public ProvidedCache(Executor providerWriteExecutor, Function<K, CompletableFuture<V>> valueProvider) {
        this(providerWriteExecutor, valueProvider, DEFAULT_LARGE_THRESHOLD);
    }

    public ProvidedCache(
            Executor providerWriteExecutor, Function<K, CompletableFuture<V>> valueProvider, int largeThreshold
    ) {
        this.providerWriteExecutor = providerWriteExecutor;
        this.valueProvider         = valueProvider;
        this.largeThreshold        = largeThreshold;
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
        return createIfAbsent ? cache.computeIfAbsent(key, Reference::new) : cache.getOrDefault(key,
                Cache.Reference.empty()
        );
    }

    @Override
    public boolean canProvide() {
        return true;
    }

    @Override
    public CompletableFuture<V> provide(K key) {
        if (containsKey(key)) {
            CompletableFuture<V> future = valueProvider.apply(key);
            future.thenAcceptAsync(it -> getReference(key, true).set(it), providerWriteExecutor);
        }

        return getReference(key, true).provider().get();
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
}
