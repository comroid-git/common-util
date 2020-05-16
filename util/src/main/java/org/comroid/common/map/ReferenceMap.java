package org.comroid.common.map;

import org.comroid.common.ref.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ReferenceMap<K, V> {
    Reference<V> getReference(K key);

    default V get(K key) {
        return getReference(key).get();
    }

    default Optional<V> wrap(K key) {
        return getReference(key).wrap();
    }

    default @NotNull V requireNonNull(K key) {
        return getReference(key).requireNonNull();
    }

    default @NotNull V requireNonNull(K key, String message) {
        return getReference(key).requireNonNull(message);
    }
}
