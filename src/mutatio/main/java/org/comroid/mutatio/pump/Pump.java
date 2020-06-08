package org.comroid.mutatio.pump;

import org.comroid.common.info.ExecutorBound;
import org.comroid.common.iter.ReferenceIndex;
import org.comroid.common.ref.Reference;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pipe.StageAdapter;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Pump<O, T> extends Pipe<O, T>, ExecutorBound {
    static <T> Pump<T, T> create() {
        return create(Runnable::run);
    }

    static <T> Pump<T, T> create(Executor executor) {
        return new BasicPump<>(executor, ReferenceIndex.create());
    }

    static <T> Pump<T, T> of(Collection<T> collection) {
        final Pump<T, T> pump = create();
        collection.forEach(pump);

        return pump;
    }

    @Override
    <R> Pump<T, R> addStage(StageAdapter<T, R> stage);

    <R> Pump<T, R> addStage(Executor executor, StageAdapter<T, R> stage);

    @Override
    default Pump<T, T> filter(Predicate<? super T> predicate) {
        return addStage(StageAdapter.filter(predicate));
    }

    @Override
    default <R> Pump<T, R> map(Function<? super T, ? extends R> mapper) {
        return addStage(StageAdapter.map(mapper));
    }

    @Override
    default <R> Pump<T, R> flatMap(Function<? super T, ? extends Reference<? extends R>> mapper) {
        return addStage(StageAdapter.flatMap(mapper));
    }

    @Override
    default Pump<T, T> distinct() {
        return addStage(StageAdapter.distinct());
    }

    @Override
    default Pump<T, T> peek(Consumer<? super T> action) {
        return addStage(StageAdapter.peek(action));
    }

    @Override
    default Pump<T, T> limit(long maxSize) {
        return addStage(StageAdapter.limit(maxSize));
    }

    @Override
    default Pump<T, T> skip(long skip) {
        return addStage(StageAdapter.skip(skip));
    }
}
