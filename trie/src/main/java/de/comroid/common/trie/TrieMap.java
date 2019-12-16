package de.comroid.common.trie;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrieMap<K extends CharSequence, V> implements Map<K, V> {
    private final Map<Character, TrieStage<V>> baseStages = new ConcurrentHashMap<>();

    @Override
    @Contract
    public int size() {
        return baseStages.values()
                .stream()
                .mapToInt(TrieStage::size)
                .sum();
    }

    @Override
    public boolean isEmpty() {
        return baseStages.isEmpty();
    }

    @Override
    @Contract
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    @Contract
    public boolean containsValue(Object value) {
        if (Objects.isNull(value))
            return false;

        return baseStages.values()
                .stream()
                .flatMap(TrieStage::streamBelow)
                .anyMatch(stage -> value.equals(stage.value));
    }

    @Override
    @Contract
    public @Nullable V get(@NotNull Object key) {
        if (!(key instanceof CharSequence))
            throw new ClassCastException(String.format("Unsupported key type: %s", key.getClass()));

        final char[] chars = Objects.requireNonNull(key, "Key cannot be null!").toString().toCharArray();

        return baseStages.computeIfAbsent(chars[0], each -> new TrieStage<>())
                .get(chars, 1);
    }

    @Override
    @Contract(mutates = "this")
    public @Nullable V put(@NotNull K key, @Nullable V value) {
        final char[] chars = Objects.requireNonNull(key, "Key cannot be null!").toString().toCharArray();

        return baseStages.computeIfAbsent(chars[0], each -> new TrieStage<>())
                .set(chars, 1, value);
    }

    @Override
    @Contract(mutates = "this")
    public @Nullable V remove(Object key) {
        if (!(key instanceof CharSequence))
            throw new ClassCastException(String.format("Unsupported key type: %s", key.getClass()));

        final char[] chars = Objects.requireNonNull(key, "Key cannot be null!").toString().toCharArray();

        @SuppressWarnings("unchecked") final K it = (K) key;
        return baseStages.computeIfAbsent(chars[0], each -> new TrieStage<>())
                .remove(chars, 1);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public void clear() {
        baseStages.clear();
    }

    @Override
    @Contract
    public @NotNull Set<K> keySet() {
        throw new UnsupportedOperationException("Gathering keys is currently not supported!");
    }

    @Override
    @Contract
    public @NotNull Collection<V> values() {
        return baseStages.values()
                .stream()
                .flatMap(TrieStage::streamBelow)
                .map(stage -> stage.value)
                .collect(Collectors.toList());
    }

    @Override
    @Contract
    public @NotNull Set<Entry<K, V>> entrySet() {
        class Local implements Map.Entry<K, V> {
            private final K key;
            private final TrieStage<V> stage;

            public Local(K key, TrieStage<V> stage) {
                this.key = key;
                this.stage = stage;
            }

            @Override
            @Contract(pure = true)
            public K getKey() {
                return key;
            }

            @Override
            @Contract(pure = true)
            public @Nullable V getValue() {
                return stage.value;
            }

            @Override
            @Contract(mutates = "this")
            public @Nullable V setValue(V value) {
                final V old = stage.value;
                stage.value = value;

                return old;
            }
        }
        //noinspection ConstantConditions
        return baseStages.values()
                .stream()
                .flatMap(TrieStage::streamBelow)
                .map(stage -> new Local(null /* gathering keys currently not supported */, stage))
                .collect(Collectors.toSet());
    }
}
