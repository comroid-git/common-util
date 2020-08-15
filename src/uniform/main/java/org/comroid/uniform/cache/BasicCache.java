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
}
