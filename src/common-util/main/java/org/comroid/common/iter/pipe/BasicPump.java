package org.comroid.common.iter.pipe;

import org.comroid.common.iter.ReferenceIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;

public class BasicPump<O, T> extends BasicPipe<O, T> implements Pump<O, T> {
    private final Collection<Pump<T, ?>> subStages = new ArrayList<>();
    private final Executor executor;

    @Override
    public Executor getExecutor() {
        return executor;
    }

    public BasicPump(ReferenceIndex<O> old) {
        this(Runnable::run, old);
    }

    public BasicPump(Executor executor, ReferenceIndex<O> old) {
        //noinspection unchecked
        this(executor, old, StageAdapter.map(it -> (T) it));
    }

    public BasicPump(Executor executor, ReferenceIndex<O> old, StageAdapter<O, T> adapter) {
        super(old, adapter, 50);

        this.executor = executor;
    }

    @Override
    public <R> Pump<T, R> addStage(StageAdapter<T, R> stage) {
        return addStage(executor, stage);
    }

    @Override
    public <R> Pump<T, R> addStage(Executor executor, StageAdapter<T, R> stage) {
        return new BasicPump<>(executor, this, stage);
    }

    @Override
    public void accept(O it) {
        refs.add(it);

        final T subData = getAdapter().apply(it);

        if (getAdapter().test(it))
            executor.execute(() -> subStages.forEach(sub -> sub.accept(subData)));
    }
}
