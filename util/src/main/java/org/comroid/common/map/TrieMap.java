package org.comroid.common.map;

import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface TrieMap<K, V> extends Map<K, V> {
    @Override
    default boolean isEmpty() {
        return size() == 0;
    }

    @Override
    @Contract
    default boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    default void putAll(@NotNull Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    static <V> TrieMap<String, V> ofString() {
        return new TrieStringMap<>(Function.identity(), Function.identity());
    }
}
