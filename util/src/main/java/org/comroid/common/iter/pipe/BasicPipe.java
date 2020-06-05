package org.comroid.common.iter.pipe;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Junction;
import org.comroid.common.iter.ReferenceIndex;
import org.comroid.common.map.TrieMap;
import org.comroid.common.ref.Reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

public class BasicPipe<O, T> implements Pipe<O, T> {
    public static final int AUTOEMPTY_DISABLED = -1;
    protected final ReferenceIndex<O> refs;
    private final Collection<Pipe<T, ?>> subs = new ArrayList<>();
    private final StageAdapter<O, T> adapter;
    private final int autoEmptyLimit;
    private final Map<Integer, Reference<T>> accessors = new TrieMap.Basic<>(Junction
            .of(String::valueOf, Integer::parseInt), false);

    @Override
    public StageAdapter<O, T> getAdapter() {
        return adapter;
    }

    public BasicPipe(ReferenceIndex<O> old) {
        this(old, 100);
    }

    public BasicPipe(ReferenceIndex<O> old, int autoEmptyLimit) {
        //noinspection unchecked
        this(old, StageAdapter.map(it -> (T) it), autoEmptyLimit);
    }

    public BasicPipe(ReferenceIndex<O> old, StageAdapter<O, T> adapter) {
        this(old, adapter, AUTOEMPTY_DISABLED);
    }

    public BasicPipe(ReferenceIndex<O> old, StageAdapter<O, T> adapter, int autoEmptyLimit) {
        this.refs = old;
        this.adapter = adapter;
        this.autoEmptyLimit = autoEmptyLimit;
    }

    @Override
    public <R> Pipe<T, R> addStage(StageAdapter<T, R> stage) {
        return new BasicPipe<>(this, stage);
    }

    @Override
    public int size() {
        return refs.size();
    }

    @Override
    public boolean add(T item) {
        if (autoEmptyLimit != AUTOEMPTY_DISABLED
                && refs.size() >= autoEmptyLimit)
            refs.clear();

        return refs.add(Polyfill.uncheckedCast(item));
    }

    @Override
    public void clear() {
        refs.clear();
    }

    @Override
    public void accept(O other) {
        refs.add(other);
    }

    @Override
    public Pipe<?, T> pipe() {
        return new BasicPipe<>(refs);
    }

    @Override
    public Reference<T> getReference(int index) {
        return accessors.computeIfAbsent(index, key -> Reference.conditional(
                () -> (index >= 0 || refs.size() >= index)
                        && adapter.test(refs.get(index)),
                () -> adapter.apply(refs.get(index))
        ));
    }
}
