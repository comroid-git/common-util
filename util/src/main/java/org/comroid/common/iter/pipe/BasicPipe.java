package org.comroid.common.iter.pipe;

import org.comroid.common.Polyfill;
import org.comroid.common.ref.Reference;
import org.comroid.common.iter.ReferenceIndex;

import java.util.ArrayList;
import java.util.Collection;

public class BasicPipe<O, T> implements Pipe<O, T> {
    protected final ReferenceIndex<O> refs;
    private final Collection<Pipe<T, ?>> subs = new ArrayList<>();
    private final StageAdapter<O, T> adapter;

    @Override
    public StageAdapter<O, T> getAdapter() {
        return adapter;
    }

    public BasicPipe(ReferenceIndex<O> old) {
        //noinspection unchecked
        this(old, StageAdapter.map(it -> (T) it));
    }

    public BasicPipe(ReferenceIndex<O> old, StageAdapter<O, T> adapter) {
        this.refs = old;
        this.adapter = adapter;
    }

    @Override
    public <R> Pipe<T, R> addStage(StageAdapter<T, R> stage) {
        return new BasicPipe<>(this, stage);
    }

    @Override
    public boolean add(T item) {
        // todo inspect
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
        return Reference.conditional(
                () -> (index >= 0 || refs.size() >= index)
                        && adapter.test(refs.get(index)),
                () -> adapter.apply(refs.get(index))
        );
    }
}
