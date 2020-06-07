package org.comroid.matrix.impl;

import org.comroid.common.map.TrieMap;
import org.comroid.matrix.Matrix;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractMatrix<V, E extends Matrix.Entry<V>> implements Matrix<V, E> {
    private final Map<String, E> entries;

    protected AbstractMatrix() {
        this(TrieMap.ofString());
    }

    protected AbstractMatrix(Map<String, E> underlying) {
        this.entries = underlying;
    }

    protected abstract @NotNull E createEntry(String key, @Nullable V initialValue);

    @Override
    public V get(String coordinate) {
        return getEntryAt(coordinate, null).get();
    }

    @Override
    public V put(String coordinate, V newValue) {
        final E entry = getEntryAt(coordinate, null);
        V old = entry.getValue();
        entry.set(newValue);

        return old;
    }

    @Override
    public @NotNull E getEntryAt(String coordinate, @Nullable V initialValue) {
        if (containsCoordinate(coordinate))
            return entries.get(coordinate);

        final E entry = createEntry(coordinate, initialValue);
        entries.put(coordinate, entry);

        return entry;
    }

    @Override
    public boolean containsCoordinate(String coordinate) {
        return entries.containsKey(coordinate);
    }

    @Nullable
    @Override
    public V compute(String coordinate, BiFunction<String, ? super V, ? extends V> computor) {
        return entries.compute(coordinate, (key, value) -> {
            if (value == null)
                value = createEntry(key, null);

            final V result = computor.apply(key, value.get());
            value.set(result);

            return value;
        }).get();
    }

    @Nullable
    @Override
    public V computeIfPresent(String coordinate, BiFunction<String, ? super V, ? extends V> computor) {
        //noinspection ConstantConditions
        return entries.computeIfPresent(coordinate, (key, value) -> {
            final V result = computor.apply(key, value.get());
            value.set(result);

            return value;
        }).get();
    }

    @Nullable
    @Override
    public V computeIfAbsent(String coordinate, Function<? super String, ? extends V> supplier) {
        return entries.computeIfAbsent(coordinate, key -> createEntry(key, supplier.apply(key))).get();
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return entries.values().iterator();
    }
}