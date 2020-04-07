package org.comroid.common.iter;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public final class MapSpan<K, V> extends AbstractMap<K, V> {
    private final Span<Map.Entry<K, V>> entries = new SpanImpl<>();

    @Override
    public V put(K key, V value) {
        final V prev = entries.stream()
                              .filter(each -> each.getKey()
                                                  .equals(key))
                              .findAny()
                              .map(Map.Entry::getValue)
                              .orElse(null);

        entries.add(new Entry(key, value));

        return prev;
    }

    @NotNull
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return entries.asSet();
    }

    private final class Entry implements Map.Entry<K, V> {
        private final K key;
        private       V value;

        private Entry(K key, V value) {
            this.key   = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V prev = this.value;
            this.value = value;

            return prev;
        }
    }
}
