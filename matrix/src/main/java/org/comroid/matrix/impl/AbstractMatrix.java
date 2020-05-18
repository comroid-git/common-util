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

    @Nullable
    @Override
    public E getEntryAt(String coordinate, boolean createIfAbsent) {
        if (containsCoordinate(coordinate))
            return entries.get(coordinate);

        if (!createIfAbsent)
            return null;

        final E entry = createEntry(coordinate, null);
        entries.put(coordinate, entry);

        return entry;
    }

    @Override
    public boolean containsCoordinate(String coordinate) {
        return entries.containsKey(coordinate);
    }

    @Nullable
    @Override
    public E compute(String coordinate, BiFunction<String, ? super E, ? extends E> computor) {
        return entries.compute(coordinate, computor);
    }

    @Nullable
    @Override
    public E computeIfPresent(String coordinate, BiFunction<String, ? super E, ? extends E> computor) {
        return entries.computeIfPresent(coordinate, computor);
    }

    @Nullable
    @Override
    public E computeIfAbsent(String coordinate, Function<? super String, ? extends E> supplier) {
        return entries.computeIfAbsent(coordinate, supplier);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return entries.values().iterator();
    }
}
