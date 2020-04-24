package org.comroid.common.map;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.comroid.common.Polyfill;
import org.comroid.common.func.EqualityComparator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrieFuncMap<K, S, V> implements TrieMap<K, V> {
    protected static class Stage<K, S, V> implements Map.Entry<K, V> {
        private final     Object                    lock = Polyfill.selfawareLock();
        private final     Stage<K, S, V>            parent;
        private final     EqualityComparator<S>     comparator;
        private final     BiFunction<S, S, Boolean> validator;
        private final     Map<S, Stage>             subStages;
        private final     K                         effectiveKey;
        private final     S                         smallKey;
        private @Nullable V                         value;

        private Stage(
                Stage<K, S, V> parent, K effectiveKey, S smallKey, EqualityComparator<S> comparator
        ) {
            this.parent       = parent;
            this.comparator   = comparator;
            this.validator    = null;
            this.subStages    = new TreeMap<>(this.comparator);
            this.effectiveKey = effectiveKey;
            this.smallKey     = smallKey;
        }

        private Stage(
                Stage<K, S, V> parent, K effectiveKey, S smallKey, BiFunction<S, S, Boolean> validator
        ) {
            this.parent       = parent;
            this.comparator   = null;
            this.validator    = validator;
            this.subStages    = new TreeMap<>(this.comparator);
            this.effectiveKey = effectiveKey;
            this.smallKey     = smallKey;
        }

        private Stage(Stage<K, S, V> parent, K effectiveKey, S smallKey) {
            this.parent       = parent;
            this.comparator   = null;
            this.validator    = null;
            this.subStages    = new TreeMap<>(this.comparator);
            this.effectiveKey = effectiveKey;
            this.smallKey     = smallKey;
        }

        @Override
        public K getKey() {
            return effectiveKey;
        }

        @Override
        public V getValue() {
            synchronized (lock) {
                return value;
            }
        }

        @Override
        public V setValue(V value) {
            synchronized (lock) {
                if (value == null) {
                    parent.subStages.remove(smallKey, this);
                }
                V prev = this.value;
                this.value = value;
                return prev;
            }
        }

        public Stream<Stage<K, S, V>> stream() {
            return subStages.values()
                    .stream()
                    .flatMap(Stage<K, S, V>::stream);
        }

        public Stream<K> streamKeys() {
            return stream().map(Stage::getKey);
        }

        public @Nullable V get(K totalKey, S[] path, int index) {
            synchronized (lock) {
                if (index >= path.length) {
                    return getValue();
                }

                return findStage(totalKey, path[index]).map(stage -> stage.get(totalKey, path, index + 1))
                        .orElse(null);
            }
        }

        public @Nullable V set(K totalKey, S[] path, int index, @Nullable V value) {
            synchronized (lock) {
                if (value == null) {
                    return remove(totalKey, path, index);
                }

                if (index >= path.length) {
                    return setValue(value);
                }

                return findStage(totalKey, path[index]).map(stage -> stage.set(totalKey, path, index + 1, value))
                        .orElse(null);
            }
        }

        public @Nullable V remove(K totalKey, S[] path, int index) {
            synchronized (lock) {
                if (index >= path.length) {
                    return setValue(null);
                }

                return findStage(totalKey, path[index]).map(stage -> stage.remove(totalKey, path, index + 1))
                        .orElse(null);
            }
        }

        public int size() {
            synchronized (lock) {
                return (int) streamKeys().count();
            }
        }

        private Optional<Stage<K, S, V>> findStage(K totalKey, S desired) {
            // todo Rework this
            synchronized (lock) {
                if (validator != null) {
                    // use validator

                    Optional<Stage<K, S, V>> any = stream().filter(stage -> validator.apply((S) stage.getKey(), desired))
                            .findAny();

                    if (!any.isPresent()) {
                        return Optional.of(subStages.computeIfAbsent(desired,
                                key -> new Stage(this, totalKey, desired, validator)
                        ));
                    }

                    return any;
                } else if (comparator != null) {
                    // use comparator

                    final Entry<S, Stage>[] entries = subStages.entrySet()
                            .toArray(new Entry[0]);
                    final S[] keys = (S[]) Arrays.stream(entries)
                            .map(Entry::getKey)
                            .toArray();
                    int index = Arrays.binarySearch(keys, desired, comparator);

                    if (index == -1) {
                        return Optional.of(subStages.computeIfAbsent(desired,
                                key -> new Stage(this, totalKey, desired, comparator)
                        ));
                    }

                    return Optional.of(entries[index].getValue());
                } else {
                    return Optional.of(subStages.computeIfAbsent(desired, key -> new Stage(this, totalKey, desired)));
                }
            }
        }
    }

    protected final TrieFuncMap.Stage<K, S, V> baseStage;
    private final   Function<K, S[]>           keySplitter;

    public TrieFuncMap(Comparator<S> comparator, Function<K, S[]> keySplitter) {
        this.keySplitter = keySplitter;
        this.baseStage   = new Stage(null, null, null, EqualityComparator.ofComparator(comparator));
    }

    public TrieFuncMap(BiFunction<S, S, Boolean> validator, Function<K, S[]> keySplitter) {
        this.keySplitter = keySplitter;
        this.baseStage   = new Stage(null, null, null, validator);
    }

    public TrieFuncMap(Function<K, S[]> keySplitter) {
        this.keySplitter = keySplitter;
        this.baseStage   = new Stage(null, null, null);
    }

    @Override
    public int size() {
        return (int) baseStage.stream()
                .count();
    }

    @Override
    public boolean containsValue(Object value) {
        return baseStage.stream()
                .anyMatch(stage -> stage.getValue()
                        .equals(value));
    }

    @Override
    public V get(Object key) {
        return baseStage.get((K) key, keySplitter.apply((K) key), 0);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return baseStage.set(key, keySplitter.apply(key), 0, value);
    }

    @Override
    public V remove(Object key) {
        return baseStage.remove((K) key, keySplitter.apply((K) key), 0);
    }

    @Override
    public void clear() {
        baseStage.subStages.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return baseStage.streamKeys()
                .collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return baseStage.stream()
                .map(Stage::getValue)
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return baseStage.stream()
                .collect(Collectors.toSet());
    }
}
