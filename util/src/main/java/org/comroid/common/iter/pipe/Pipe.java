package org.comroid.common.iter.pipe;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Disposable;
import org.comroid.common.ref.Reference;
import org.comroid.common.iter.ReferenceIndex;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Pipe<O, T> extends ReferenceIndex<T>, Consumer<O>, Disposable {
    StageAdapter<O, T> getAdapter();

    static <T> Pipe<T, T> create() {
        return new BasicPipe<>(ReferenceIndex.create());
    }

    static <T> Pipe<T, T> of(Collection<T> collection) {
        final Pipe<T, T> pipe = create();
        collection.forEach(pipe);

        return pipe;
    }

    <R> Pipe<T, R> addStage(StageAdapter<T, R> stage);

    default Pipe<T, T> filter(Predicate<? super T> predicate) {
        return addStage(StageAdapter.filter(predicate));
    }

    default <R> Pipe<T, R> map(Function<? super T, ? extends R> mapper) {
        return addStage(StageAdapter.map(mapper));
    }

    default <R> Pipe<T, R> flatMap(Function<? super T, ? extends Reference<? extends R>> mapper) {
        return addStage(StageAdapter.flatMap(mapper));
    }

    default Pipe<T, T> distinct() {
        return addStage(StageAdapter.distinct());
    }

    default Pipe<T, T> peek(Consumer<? super T> action) {
        return addStage(StageAdapter.peek(action));
    }

    default Pipe<T, T> limit(long maxSize) {
        return addStage(StageAdapter.limit(maxSize));
    }

    default Pipe<T, T> skip(long skip) {
        return addStage(StageAdapter.skip(skip));
    }

    default void forEach(Consumer<? super T> action) {
        addStage(StageAdapter.peek(action));
    }

    @Override
    default boolean remove(T item) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("remove() is not supported by pipe");
    }

    @Override
    default Pump<O, T> pump(Executor executor) {
        return new BasicPump<>(executor, this.map(Polyfill::uncheckedCast));
    }

    @Override
    default void accept(O o) {
        add(getAdapter().apply(o));
    }
}
