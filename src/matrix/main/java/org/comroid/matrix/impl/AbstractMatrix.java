package org.comroid.matrix.impl;

import org.comroid.api.UUIDContainer;
import org.comroid.matrix.Matrix;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.ref.KeyedReference;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractMatrix<V, E extends Matrix.Entry<V>> extends UUIDContainer.Base implements Matrix<V, E> {
    private final Map<String, E> entries;
    private final EntryIndex entryIndex = new EntryIndex();

    protected AbstractMatrix() {
        this(new ConcurrentHashMap<>());
    }

    protected AbstractMatrix(Map<String, E> underlying) {
        this.entries = Optional.ofNullable(underlying).orElseGet(ConcurrentHashMap::new);
    }

    @Override
    public Stream<? extends KeyedReference<String, V>> streamRefs() {
        return entries.values().stream();
    }

    protected abstract @NotNull E createEntry(String key, @Nullable V initialValue);

    @Override
    public V get(String coordinate) {
        return getEntryAt(coordinate, null).get();
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean containsKey(String key) {
        return entries.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return stream().anyMatch(value::equals);
    }

    @Override
    public Pipe<? extends KeyedReference<String, V>> pipe(Predicate<String> filter) {
        return entryIndex.pipe();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super V> action) {
        biPipe().forEach(action);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public boolean put(String coordinate, V newValue) {
        final E entry = getEntryAt(coordinate, null);
        V old = entry.getValue();
        entry.set(newValue);

        return old != newValue;
    }

    @Override
    public @Nullable KeyedReference<String, V> getReference(String key, boolean createIfAbsent) {
        return entries.get(key);
    }

    @Override
    public ReferenceIndex<? extends KeyedReference<String, V>> entryIndex() {
        return entryIndex;
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

    @Override
    public @Nullable V remove(String coordinate) {
        final E entry = entries.get(coordinate);
        V prev;

        if (entry == null || (prev = entry.getValue()) == null)
            return null;
        if (entry.set(null))
            return prev;
        else throw new UnsupportedOperationException("Could not unset entry");
    }


    @NotNull
    @Override
    public Iterator<E> iterator() {
        return entries.values().iterator();
    }

    @Override
    public boolean isNull(String coordinate) {
        return getEntryAt(coordinate, null).isNull();
    }

    private final class EntryIndex implements ReferenceIndex<E> {
        private final Map<Integer, Reference<E>> accessors = new ConcurrentHashMap<>();

        @Override
        public List<E> unwrap() {
            return new ArrayList<>(entries.values());
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public boolean add(E item) {
            return put(item.getCoordinate(), item.getValue());
        }

        @Override
        public boolean remove(E item) {
            return AbstractMatrix.this.remove(item.getCoordinate()) != item.getValue();
        }

        @Override
        public void clear() {
            AbstractMatrix.this.clear();
        }

        @Override
        public Stream<? extends Reference<E>> streamRefs() {
            return accessors.values().stream();
        }

        @Override
        public Reference<E> getReference(int index) {
            return accessors.computeIfAbsent(index, k -> Reference
                    .provided(this::unwrap)
                    .map(list -> list.get(k)));
        }
    }
}
