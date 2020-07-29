package org.comroid.trie;

import org.comroid.api.Junction;
import org.comroid.api.Polyfill;
import org.comroid.mutatio.ref.KeyedReference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public final class TrieArrayMap<K, V> extends TrieStage<K, V> implements TrieMap<K, V> {
    private final Junction<K, String> converter;

    @Override
    public Junction<K, String> getKeyConverter() {
        return converter;
    }

    public TrieArrayMap(Function<String, K> toKey) {
        this(Object::toString, toKey);
    }

    public TrieArrayMap(Function<K, String> toString, Function<String, K> toKey) {
        super(null, "");

        this.converter = Junction.of(toString, toKey);
    }

    @Override
    public void printStages() {
//todo
    }

    @Override
    public @Nullable KeyedReference<K, V> getReference(K key, boolean createIfAbsent) {
        Objects.requireNonNull(key, "Key");

        final String keyConverted = getKeyConverter().forward(key);
        return getReference(keyConverted, keyConverted.toCharArray(), 0, createIfAbsent);
    }

    @Override
    public ReferenceIndex<Entry<K, V>> entryIndex() {
        return null;//todo
    }

    @Override
    public int size() {
        return 0;//todo
    }

    @Override
    public boolean containsKey(Object key) {
        return getReference(Polyfill.uncheckedCast(key), false) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;//todo
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return getReference(key, true).setValue(value);
    }

    @Override
    public V remove(Object key) {
        final KeyedReference<K, V> ref = getReference(Polyfill.uncheckedCast(key), false);

        return ref != null ? ref.setValue(null) : null;
    }

    @Override
    public void clear() {
        // todo
    }
}
