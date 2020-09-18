package org.comroid.trie;

import org.comroid.api.Junction;
import org.comroid.api.Polyfill;
import org.comroid.mutatio.ref.KeyedReference;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.mutatio.ref.ReferenceMap;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface TrieMap<K, V> extends ReferenceMap<K, V> {
    Junction<K, String> getKeyConverter();

    default boolean isEmpty() {
        return size() == 0;
    }

    static <V> TrieMap<String, V> ofString() {
        return new TrieArrayMap<>(String::intern);
    }

    static <K, V> TrieMap<K, V> parsing(Function<String, K> parser) {
        return new TrieArrayMap<>(parser);
    }

    default void putAll(@NotNull Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    default V get(Object key) {
        // override because interfaces are retarded

        //noinspection unchecked
        return getReference((K) key).get();
    }

    @NotNull
    default Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(new HashSet<>(entryIndex().unwrap()));
    }

    @NotNull
    default Set<K> keySet() {
        //noinspection SimplifyStreamApiCallChains
        return Collections.unmodifiableSet(entrySet()
                .stream()
                .map(Entry::getKey)
                .collect(Collectors.toSet()));
    }

    @NotNull
    default Collection<V> values() {
        return entryIndex().pipe()
                .map(Entry::getValue)
                .span();
    }

    @Experimental
    void printStages();
}
