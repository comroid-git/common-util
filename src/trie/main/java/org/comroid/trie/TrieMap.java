package org.comroid.trie;

import org.comroid.api.Junction;
import org.comroid.api.Polyfill;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.mutatio.ref.ReferenceMap;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
        return new Basic<>(Junction.of(it -> String.valueOf(it).intern(), parser), true);
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

    @NotNull
    @Override
    default Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(new HashSet<>(entryIndex().unwrap()));
    }

    @NotNull
    @Override
    default Set<K> keySet() {
        //noinspection SimplifyStreamApiCallChains
        return Collections.unmodifiableSet(entrySet()
                .stream()
                .map(Entry::getKey)
                .collect(Collectors.toSet()));
    }

    @NotNull
    @Override
    default Collection<V> values() {
        return entryIndex().pipe()
                .map(Entry::getValue)
                .span();
    }

    @Experimental
    void printStages();

    final class Stage<V> implements Map.Entry<String, V> {
        private final Map<Character, Stage<V>> storage = new ConcurrentHashMap<>();
        private final Reference.Settable<V> reference = Reference.Settable.create();
        private final Stage<V> parent;
        private final String keyConverted;
        private final char c;

        @Override
        public String getKey() {
            return keyConverted;
        }

        public String getDescribedKey() {
            if (parent == null)
                return keyConverted;
            return parent.getDescribedKey() + File.separatorChar + c;
        }

        @Override
        public V getValue() {
            return reference.get();
        }

        private Stage(Stage<V> parent, char c, String keyConverted) {
            this.parent = parent;
            this.keyConverted = keyConverted;
            this.c = c;
        }

        private Stage(Stage<V> parent, char c, String keyConverted, V containValue) {
            this(parent, c, keyConverted);

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

        private Optional<Reference.Settable<V>> getReference(String targetKey, char[] chars, int cIndex) {
            if (cIndex >= chars.length)
                return Optional.of(reference);

            return requireStage(targetKey, chars, cIndex)
                    .getReference(targetKey, chars, cIndex + 1);
        }

        private Optional<V> putValue(String targetKey, char[] chars, int cIndex, @Nullable V value) {
            if (cIndex >= chars.length)
                return Optional.ofNullable(setValue(value));

            return requireStage(targetKey, chars, cIndex)
                    .putValue(targetKey, chars, cIndex + 1, value);
        }

        private Optional<V> remove(String targetKey, char[] chars, int cIndex) {
            if (cIndex >= chars.length)
                return Optional.ofNullable(remove());

            return requireStage(targetKey, chars, cIndex)
                    .remove(targetKey, chars, cIndex);
        }

        public boolean containsKey(String targetKey, char[] chars, int cIndex) {
            if (cIndex >= chars.length)
                return false;

            return requireStage(targetKey, chars, cIndex)
                    .containsKey(targetKey, chars, cIndex + 1);
        }

        public Stage<V> requireStage(String targetKey, char[] chars, int cIndex) {
            if (targetKey.equals(keyConverted) || cIndex >= chars.length)
                return this;

            return storage.computeIfAbsent(chars[cIndex], key -> {
                String converted = new String(Arrays.copyOfRange(chars, 0, cIndex + 1));
                return new Stage<>(this, chars[cIndex], converted);
            }).requireStage(targetKey, chars, cIndex + 1);
        }

        @Override
        public String toString() {
            return String.format("Stage{keyConverted='%s'}", keyConverted);
        }

        @Experimental
        private Stream<Stage<V>> streamStages() {
            return Stream.concat(
                    Stream.of(this),
                    storage.values()
                            .stream()
                            .flatMap(Stage::streamStages)
            );
        }
    }

    class Basic<K, V> implements TrieMap<K, V> {
        private final TrieMap.Stage<V> baseStage = new Stage<>(null, (char) 0, null);
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
        public void printStages() {
            baseStage.streamStages()
                    .map(stage -> stage.reference.isNull()
                            ? String.format("%s", stage.getDescribedKey())
                            : String.format("%s -> %s", stage.getDescribedKey(), stage.reference.get()))
                    .forEachOrdered(System.out::println);
        }

        @Override
        public Stage<V> getStage(String key) {
            return baseStage.requireStage(key, key.toCharArray(), 0);
        }

        @Override
        public boolean containsKey(Object key) {
            final char[] convertKey = convertKey(key);
            final String kStr = new String(convertKey);
            final Stage<V> stage = baseStage.requireStage(kStr, convertKey, 0);

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

            final String kStr = new String(convertKey);
            return baseStage.getReference(kStr, convertKey, 0)
                    .orElseGet(Reference.Settable::create);
        }

        @Override
        public @NotNull Set<Entry<K, V>> entrySet() {
            return baseStage.streamPresentStages()
                    .map(stage -> new AbstractMap.SimpleImmutableEntry<>(
                            keyConverter.backward(stage.keyConverted),
                            stage.reference.get()
                    ))
                    .collect(Collectors.toSet());
        }

        @Override
        public @NotNull Set<K> keySet() {
            //noinspection SimplifyStreamApiCallChains
            return entrySet().stream()
                    .map(Entry::getKey)
                    .collect(Collectors.toSet());
        }

        @Override
        public @NotNull Collection<V> values() {
            //noinspection SimplifyStreamApiCallChains
            return entrySet().stream()
                    .map(Entry::getValue)
                    .collect(Collectors.toList());
        }

        @Override
        public ReferenceIndex<Entry<K, V>> entryIndex() {
            return new EntryIndex();
        }

        @Nullable
        @Override
        public V put(K key, V value) {
            final char[] chars = convertKey(key);
            final String kStr = new String(chars);

            if (!containsKey(key))
                return baseStage.requireStage(kStr, chars, 0)
                        .reference.set(value);
            return baseStage.putValue(kStr, chars, 0, value).orElse(null);
        }

        @Override
        public V remove(Object key) {
            final char[] chars = convertKey(key);
            final String kStr = new String(chars);

            return baseStage.remove(kStr, chars, 0).orElse(null);
        }

        @Override
        public void clear() {
            baseStage.storage.clear();
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

        private final class EntryIndex implements ReferenceIndex<Entry<K, V>> {
            @Override
            public List<Entry<K, V>> unwrap() {
                return new ArrayList<>(entrySet());
            }

            @Override
            public int size() {
                return Basic.this.size();
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
                        () -> Basic.this.size() < index,
                        () -> unwrap().get(index)
                );
            }
        }
    }
}
