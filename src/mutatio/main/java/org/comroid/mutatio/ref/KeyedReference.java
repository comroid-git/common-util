package org.comroid.mutatio.ref;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface KeyedReference<K, V> extends Reference<V>, Map.Entry<K, V> {
    static <K, V> KeyedReference<K, V> create(K key) {
        return create(key, null);
    }

    static <K, V> KeyedReference<K, V> create(K key, @Nullable V initialValue) {
        return create(true, key, initialValue);
    }

    static <K, V> KeyedReference<K, V> create(boolean mutable, K key) {
        return create(mutable, key, null);
    }

    static <K, V> KeyedReference<K, V> create(boolean mutable, K key, @Nullable V initialValue) {
        return new Basic<>(mutable, key, initialValue);
    }

    final class Basic<K, V> extends Reference.Support.Base<V> implements KeyedReference<K, V> {
        private final K key;
        private final Reference<V> valueHolder;

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return get();
        }

        private Basic(boolean mutable, K key, @Nullable V initialValue) {
            super(mutable);

            this.key = key;
            this.valueHolder = Reference.create(initialValue);
        }

        @Override
        public V setValue(V value) {
            V prev = get();

            return set(value) ? prev : null;
        }

        @Override
        protected V doGet() {
            return valueHolder.get();
        }

        @Override
        protected boolean doSet(V value) {
            return valueHolder.set(value);
        }
    }
}
