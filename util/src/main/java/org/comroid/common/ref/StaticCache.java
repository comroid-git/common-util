package org.comroid.common.ref;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class StaticCache {
    private static final Map<Object, StaticCache> staticCache = new ConcurrentHashMap<>();
    private final        Map<Class<?>, Object>    cache       = new ConcurrentHashMap<>();
    private final        Object                   owner;

    private StaticCache(Object owner) {
        this.owner = owner;
    }

    public static <T> T access(Object accessor, Class<T> cacheType, Supplier<T> supplier) {
        return myCache(accessor).computeIfAbsent(cacheType, supplier);
    }

    public static StaticCache myCache(Object accessor) {
        return staticCache.computeIfAbsent(accessor, StaticCache::new);
    }

    private <T> T computeIfAbsent(Class<T> type, Supplier<T> supplier) {
        final Object value = cache.computeIfAbsent(type, (key) -> supplier.get());

        if (!type.isInstance(value))
            throw new AssertionError();

        return type.cast(value);
    }
}
