package org.comroid.common.map;

import org.comroid.common.ref.Pair;

import java.util.Map;

public final class EntryPair<K, V> extends Pair<K, V> implements Map.Entry<K, V> {
    @Override
    public K getKey() {
        return getFirst();
    }

    @Override
    public V getValue() {
        return getSecond();
    }

    public EntryPair(K first) {
        this(first, null);
    }

    public EntryPair(K first, V second) {
        super(first, second);
    }

    @Override
    public V setValue(V value) {
        final V prev = getSecond();
        super.second.set(value);
        return prev;
    }
}
