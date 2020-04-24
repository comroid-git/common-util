package org.comroid.common.map;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public interface ReferenceMap<K, V> {
    default Optional<V> wrap(K key) {
        return Optional.ofNullable(get(key));
    }

    V get(K key);

    default @NotNull V requireNonNull(K key) {
        return Objects.requireNonNull(get(key));
    }

    default @NotNull V requireNonNull(K key, String message) {
        return Objects.requireNonNull(get(key), message);
    }
}
