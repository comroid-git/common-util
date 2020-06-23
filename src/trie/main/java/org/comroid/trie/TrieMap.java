package org.comroid.trie;

import org.comroid.api.Junction;
import org.comroid.api.Polyfill;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.mutatio.ref.ReferenceMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface TrieMap<K, V> extends ReferenceMap<K, V, Reference.Settable<V>>, Map<K, V> {
    Junction<K, String> getKeyConverter();

    @Override
    default boolean isEmpty() {
        return size() == 0;
    }

    static <V> TrieMap<String, V> ofString() {
        return new Basic<>(Junction.of(String::intern, Function.identity()), true);
    }

    static <K, V> TrieMap<K, V> parsing(Function<String, K> parser) {
        return new Basic<>(Junction.of(Objects::toString, parser), false);
    }

    static <K, V> TrieMap<K, V> parsingCached(Function<String, K> parser) {
        return new Basic<>(Junction.of(Objects::toString, parser), true);
    }

    @Override
    default void putAll(@NotNull Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    Stage<V> getStage(String key);

    default V get(Object key) {
        //noinspection unchecked
        return getReference((K) key).get();
    }

    final class Stage<V> implements Map.Entry<String, V> {
        private final Map<Character, Stage<V>> storage = new ConcurrentHashMap<>();
        private final Reference.Settable<V> reference = Reference.Settable.create();
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
        }

        private Stage(String keyConverted, V containValue) {
            this(keyConverted);

            this.reference.set(containValue);
        }

        public V remove() {
            V prev = reference.get();
            reference.set(null);
            return prev;
        }

        @Override
        public V setValue(V value) {
            if (value == null)
                return remove();

            return reference.set(value);
        }

        private Stream<Stage<V>> streamPresentStages() {
            if (reference.isNull())
                return storage.values()
                        .stream()
                        .flatMap(Stage::streamPresentStages);

            return Stream.concat(
                    Stream.of(this),
                    storage.values().stream().flatMap(Stage::streamPresentStages)
            );
        }

        private Optional<Reference.Settable<V>> getReference(char[] chars, int cIndex) {
            if (cIndex >= chars.length)
                return Optional.of(reference);

            return getStageByChar(chars[cIndex])
                    .flatMap(stage -> stage.getReference(chars, cIndex + 1));
        }

        private Optional<V> putValue(char[] chars, int cIndex, @Nullable V value) {
            if (cIndex >= chars.length)
                return Optional.ofNullable(setValue(value));

            // expect existing stages
            return getStageByChar(chars[cIndex])
                    .flatMap(stage -> stage.putValue(chars, cIndex + 1, value));
        }

        private Optional<V> remove(char[] chars, int cIndex) {
            if (cIndex >= chars.length)
                return Optional.ofNullable(remove());

            return getStageByChar(chars[cIndex])
                    .flatMap(stage -> stage.remove(chars, cIndex));
        }

        public boolean containsKey(char[] chars, int cIndex) {
            if (cIndex >= chars.length)
                return false;

            return getStageByChar(chars[cIndex])
                    .map(stage -> stage.containsKey(chars, cIndex + 1))
                    .orElse(false);
        }

        public Stage<V> requireStage(char[] chars, int cIndex) {
            if (cIndex < chars.length) {
                return storage.computeIfAbsent(chars[cIndex], key -> {
                    String converted = new String(Arrays.copyOfRange(chars, 0, cIndex + 1));
                    return new Stage<>(converted);
                }).requireStage(chars, cIndex + 1);
            } else return this;
        }

        @NotNull
        public Optional<Stage<V>> getStageByChar(char aChar) {
            return Optional.ofNullable(storage.getOrDefault(aChar, null));
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
            final char[] convertKey = convertKey(key);
            final Stage<V> stage = baseStage.requireStage(convertKey, 0);

            return Arrays.equals(stage.keyConverted.toCharArray(), convertKey)
                    && !stage.reference.isNull();
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
        public Reference.Settable<V> getReference(K key, boolean createIfAbsent) {
            final char[] convertKey = convertKey(key);

            if (convertKey.length == 0)
                return Reference.Settable.create();

            return baseStage.getReference(convertKey, 0)
                    .orElseGet(Reference.Settable::create);
        }

        @Override
        public ReferenceIndex<Entry<K, V>> entryIndex() {
            class RemoteIndex implements ReferenceIndex<Entry<K, V>> {
                private final ArrayList<Entry<K, V>> entries = new ArrayList<>(entrySet());

                @Override
                public List<Entry<K, V>> unwrap() {
                    return entries;
                }

                @Override
                public int size() {
                    return entries.size();
                }

                @Override
                public boolean add(Entry<K, V> entry) {
                    Basic.this.put(entry.getKey(), entry.getValue());
                    return Basic.this.containsKey(entry.getKey());
                }

                @Override
                public boolean remove(Entry<K, V> entry) {
                    Basic.this.remove(entry.getKey());
                    return !Basic.this.containsKey(entry.getKey());
                }

                @Override
                public void clear() {
                    Basic.this.clear();
                }

                @Override
                public Reference<Entry<K, V>> getReference(int index) {
                    return Reference.conditional(
                            () -> entries.size() < index,
                            () -> entries.get(index)
                    );
                }
            }

            return new RemoteIndex();
        }

        @Nullable
        @Override
        public V put(K key, V value) {
            if (!containsKey(key))
                return baseStage.requireStage(convertKey(key), 0)
                        .reference.set(value);
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
            return baseStage.streamPresentStages()
                    .map(Stage::getKey)
                    .map(getKeyConverter()::backward)
                    .collect(Collectors.toSet());
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
            return Collections.unmodifiableSet(baseStage.streamPresentStages()
                    .map(stage -> new AbstractMap.SimpleImmutableEntry<>(
                            getKeyConverter().backward(stage.keyConverted),
                            stage.reference.get()
                    ))
                    .collect(Collectors.toSet()));
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
