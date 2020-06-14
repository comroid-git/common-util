package org.comroid.trie;

import org.comroid.api.Junction;
import org.comroid.api.Polyfill;
import org.comroid.mutatio.ref.OutdateableReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface TrieMap<K, V> extends Map<K, V> {
    Junction<K, String> getKeyConverter();

    @Override
    default boolean isEmpty() {
        return size() == 0;
    }

    static <V> TrieMap<String, V> ofString() {
        return new Basic<>(Junction.identity(), false);
    }

    @Override
    default void putAll(@NotNull Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    Stage<V> getStage(String key);

    final class Stage<V> implements Map.Entry<String, V> {
        private final Map<Character, Stage<V>> storage = new ConcurrentHashMap<>();
        private final OutdateableReference<V> reference = new OutdateableReference<>();
        private final String keyConverted;

        @Override
        public String getKey() {
            return keyConverted;
        }

        @Override
        public V getValue() {
            return reference.get();
        }

        private Stage(String keyConverted) {
            this.keyConverted = keyConverted;
            this.reference.outdate();
        }

        private Stage(String keyConverted, V containValue) {
            this(keyConverted);

            this.reference.update(containValue);
        }

        public V remove() {
            reference.outdate();
            return reference.get();
        }

        @Override
        public V setValue(V value) {
            if (value == null)
                return remove();

            return reference.update(value);
        }

        private Stream<Stage<V>> streamPresentStages() {
            return Stream.concat(
                    reference.isOutdated() ? Stream.empty() : Stream.of(this),
                    storage.values().stream().flatMap(Stage::streamPresentStages)
            );
        }

        private Optional<V> getValue(char[] chars, int cIndex) {
            if (cIndex >= chars.length) {
                if (!reference.isOutdated())
                    return reference.wrap();
                else return Optional.empty();
            }

            return Optional.ofNullable(storage.getOrDefault(chars[cIndex], null))
                    .flatMap(stage -> stage.getValue(chars, cIndex + 1));
        }

        private Optional<V> putValue(char[] chars, int cIndex, @Nullable V value) {
            if (cIndex >= chars.length)
                return Optional.ofNullable(setValue(value));

            // expect existing stages
            return Optional.ofNullable(storage.getOrDefault(chars[cIndex], null))
                    .flatMap(stage -> stage.putValue(chars, cIndex + 1, value));
        }

        private Optional<V> remove(char[] chars, int cIndex) {
            if (cIndex >= chars.length)
                return Optional.ofNullable(remove());

            return Optional.ofNullable(storage.getOrDefault(chars[cIndex], null))
                    .flatMap(stage -> stage.remove(chars, cIndex));
        }

        public boolean containsKey(char[] chars, int cIndex) {
            if (cIndex >= chars.length)
                return false;
            if (cIndex == chars.length - 1)
                return storage.containsKey(chars[cIndex]);

            return Optional.ofNullable(storage.getOrDefault(chars[cIndex], null))
                    .map(stage -> stage.containsKey(chars, cIndex + 1))
                    .orElse(false);
        }

        private Stage<V> requireStage(char[] chars, int cIndex) {
            if (cIndex < chars.length) {
                return storage.computeIfAbsent(chars[cIndex], key -> {
                    String converted = new String(Arrays.copyOfRange(chars, 0, cIndex + 1));
                    return new Stage<>(converted);
                }).requireStage(chars, cIndex + 1);
            } else return this;
        }
    }

    class Basic<K, V> implements TrieMap<K, V> {
        private final TrieMap.Stage<V> baseStage = new Stage<>(null);
        private final Map<K, String> cachedKeys = new ConcurrentHashMap<>();
        private final Junction<K, String> keyConverter;
        private final boolean useKeyCache;

        @Override
        public Junction<K, String> getKeyConverter() {
            return keyConverter;
        }

        public Basic(Junction<K, String> keyConverter, boolean useKeyCache) {
            this.keyConverter = keyConverter;
            this.useKeyCache = useKeyCache;
        }

        @Override
        public Stage<V> getStage(String key) {
            return baseStage.requireStage(key.toCharArray(), 0);
        }

        @Override
        public boolean containsKey(Object key) {
            return baseStage.containsKey(convertKey(key), 0);
        }

        @Override
        public int size() {
            return (int) baseStage.streamPresentStages().count();
        }

        @Override
        public boolean containsValue(Object value) {
            return baseStage.streamPresentStages().anyMatch(value::equals);
        }

        @Override
        public V get(Object key) {
            final char[] convertKey = convertKey(key);

            if (convertKey.length == 0)
                return null;

            return baseStage.getValue(convertKey, 0).orElse(null);
        }

        @Nullable
        @Override
        public V put(K key, V value) {
            if (!assertStageExists(key))
                throw new AssertionError("Could not generate Stage for key: " + key);

            return baseStage.putValue(convertKey(key), 0, value).orElse(null);
        }

        @Override
        public V remove(Object key) {
            return baseStage.remove(convertKey(key), 0).orElse(null);
        }

        @Override
        public void clear() {
            baseStage.storage.clear();
        }

        @NotNull
        @Override
        public Set<K> keySet() {
            return cachedKeys.keySet();
        }

        @NotNull
        @Override
        public Collection<V> values() {
            return Collections.unmodifiableList(baseStage.streamPresentStages()
                    .map(Stage::getValue)
                    .collect(Collectors.toList()));
        }

        @NotNull
        @Override
        public Set<Entry<K, V>> entrySet() {
            return cachedKeys.entrySet()
                    .stream()
                    .filter(entry -> containsKey(entry.getKey()))
                    .map(entry -> {
                        String converted = entry.getValue();
                        return new AbstractMap.SimpleImmutableEntry<>(
                                entry.getKey(),
                                baseStage.getValue(converted.toCharArray(), 0)
                                        .orElseThrow(() -> new AssertionError("Inexistent stage: " + converted))
                        );
                    }).collect(Collectors.toSet());
        }

        private boolean assertStageExists(Object atKey) {
            if (containsKey(atKey))
                return true;

            //noinspection unchecked
            final K key = (K) atKey;
            final char[] converted = convertKey(atKey);
            final char baseKey = converted[0];
            final String convertedKey = new String(converted);

            IntStream.range(0, converted.length + 1)
                    .mapToObj(end -> Arrays.copyOfRange(converted, 0, end))
                    .filter(chars -> !containsKey(new String(chars)))
                    .forEachOrdered(chars -> baseStage.requireStage(chars, 0));

            return containsKey(convertedKey);
        }

        private char[] convertKey(Object key) {
            return cacheKey(key).toCharArray();
        }

        private String cacheKey(Object key) {
            if (key instanceof String)
                return (String) key;

            final K keyCast = Polyfill.uncheckedCast(key);

            return useKeyCache
                    ? cachedKeys.computeIfAbsent(keyCast, it -> getKeyConverter().forward(it))
                    : getKeyConverter().forward(keyCast);
        }
    }
}
