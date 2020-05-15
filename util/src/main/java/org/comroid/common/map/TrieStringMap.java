package org.comroid.common.map;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TrieStringMap<K, V> implements TrieMap<K, V> {
    private final Map<Character, TrieStage<V>> baseStages = new ConcurrentHashMap<>();
    private final Function<K, String> keyExtractor;
    private final Function<String, K>          keyMapper;

    public TrieStringMap(Function<K, String> keyExtractor, Function<String, K> keyMapper) {
        this.keyExtractor = keyExtractor;
        this.keyMapper = keyMapper;
    }

    @Override
    @Contract
    public int size() {
        return baseStages.values()
                .stream()
                .mapToInt(TrieStringMap.TrieStage::size)
                .sum();
    }

    @Override
    @Contract
    public boolean containsValue(Object value) {
        if (Objects.isNull(value)) {
            return false;
        }

        return baseStages.values()
                .stream()
                .flatMap(TrieStringMap.TrieStage::stream)
                .anyMatch(stage -> value.equals(stage.value));
    }

    @Override
    @Contract
    public V get(@NotNull Object key) {
        if (!(key instanceof CharSequence)) {
            throw new ClassCastException(String.format("Unsupported key type: %s", key.getClass()));
        }

        final char[] chars = Objects.requireNonNull(key, "Key cannot be null!")
                .toString()
                .toCharArray();

        return baseStages.computeIfAbsent(chars[0], each -> new TrieStringMap.TrieStage<>())
                .get(chars, 1);
    }

    @Override
    @Contract(mutates = "this")
    public V put(K key, V value) {
        final char[] chars = keyExtractor.apply(key).toCharArray();

        return baseStages.computeIfAbsent(chars[0], each -> new TrieStringMap.TrieStage<>())
                .set(chars, 1, value);
    }

    @Override
    @Contract(mutates = "this")
    public V remove(Object key) {
        if (!(key instanceof CharSequence)) {
            throw new ClassCastException(String.format("Unsupported key type: %s", key.getClass()));
        }

        //noinspection unchecked
        final char[] chars = keyExtractor.apply((K) key).toCharArray();

        return baseStages.computeIfAbsent(chars[0], each -> new TrieStringMap.TrieStage<>())
                .remove(chars, 1);
    }

    @Override
    public void clear() {
        baseStages.clear();
    }

    /**
     * @return A Set of generated Strings representing all contained keys.
     */
    @Override
    @Contract
    @NotNull
    public Set<K> keySet() {
        class Pair {
            final String                     key;
            final TrieStringMap.TrieStage<V> stage;
            Pair(String key, TrieStringMap.TrieStage<V> stage) {
                this.key   = key;
                this.stage = stage;
            }
        }

        return baseStages.entrySet()
                .stream()
                .map(entry -> new Pair(entry.getKey()
                        .toString(), entry.getValue()))
                .flatMap(pair -> pair.stage.streamKeys(pair.key))
                .map(keyMapper)
                .collect(Collectors.toSet());
    }

    @Override
    @Contract
    @NotNull
    public Collection<V> values() {
        return baseStages.values()
                .stream()
                .flatMap(TrieStringMap.TrieStage::stream)
                .map(stage -> stage.value)
                .collect(Collectors.toList());
    }

    @Override
    @Contract
    @NotNull
    public Set<Entry<K, V>> entrySet() {
        class Local implements Entry<K, V> {
            private final K                          key;
            private final TrieStringMap.TrieStage<V> stage;

            public Local(K key, TrieStringMap.TrieStage<V> stage) {
                this.key   = key;
                this.stage = stage;
            }

            @Override
            @Contract(pure = true)
            public K getKey() {
                return key;
            }

            @Override
            @Contract(pure = true)
            public V getValue() {
                return stage.value;
            }

            @Override
            @Contract(mutates = "this")
            public V setValue(V value) {
                final V old = stage.value;
                stage.value = value;

                return old;
            }
        }
        return baseStages.values()
                .stream()
                .flatMap(TrieStringMap.TrieStage::stream)
                .map(stage -> new Local(null /* gathering keys currently not supported */, stage))
                .collect(Collectors.toSet());
    }

    //region Stage Class
    private static class TrieStage<V> {
        private final     Map<Character, TrieStage<V>> subStages = new ConcurrentHashMap<>();

        @Nullable V get(char[] chars, int index) {
            if (chars.length == 0 || index >= chars.length) {
                return value;
            }

            return subStages.computeIfAbsent(chars[index], it -> new TrieStage<>())
                    .get(chars, index + 1);
        }

        @Nullable V set(char[] chars, int index, @Nullable V value) {
            if (chars.length == 0 || index >= chars.length) {
                final V old = this.value;
                this.value = value;

                return old;
            }

            return subStages.computeIfAbsent(chars[index], it -> new TrieStage<>())
                    .set(chars, index + 1, value);
        }

        @Nullable V remove(char[] chars, int index) {
            if (chars.length == 0 || index + 1 >= chars.length) {
                return subStages.remove(chars[index + 1]).value;
            }

            return subStages.computeIfAbsent(chars[index], it -> new TrieStage<>())
                    .remove(chars, index + 1);
        }

        int size() {
            return subStages.values()
                    .stream()
                    .mapToInt(TrieStage::size)
                    .sum() + (
                    Objects.isNull(value) ? 0 : 1
            );
        }

        Stream<String> streamKeys(String base) {
            return Stream.concat(
                    Stream.of(base),
                    subStages.entrySet()
                            .stream()
                            .flatMap(entry -> entry.getValue()
                                    .streamKeys(base + entry.getKey()))
            );
        }

        Stream<TrieStage<V>> stream() {
            return Stream.concat(
                    Stream.of(this),
                    subStages.values()
                            .stream()
                            .flatMap(TrieStage::stream)
            );
        }
        private @Nullable V                            value;
    }
}
