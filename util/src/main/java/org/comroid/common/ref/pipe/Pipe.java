package org.comroid.common.ref.pipe;

import org.comroid.common.ref.Reference;
import org.comroid.common.ref.ReferenceIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Pipe<O, T> extends ReferenceIndex<T>, Consumer<O>, AutoCloseable {
    static <T> Pipe<T, T> create() {
        return create(ForkJoinPool.commonPool());
    }

    static <T> Pipe<T, T> create(Executor executor) {
        return new Support.Basic<>(executor, ReferenceIndex.of(new ArrayList<>()));
    }

    static <T> Pipe<T, T> of(Collection<T> collection) {
        final Pipe<T, T> pipe = create();
        collection.forEach(pipe);

        return pipe;
    }

    Optional<StageAdapter<?, T>> getAdapter();

    Executor getExecutor();

    @Override
    void close();

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
        close();
    }

    @Override
    default boolean add(T item) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("add() is not supported by pipe, use accept() instead");
    }

    final class Support {
        private static final class Basic<O, T> implements Pipe<O, T> {
            protected final Executor executor;
            private final ReferenceIndex<O> refs;
            private final StageAdapter<O, T> adapter;
            private boolean closed = false;

            @Override
            public Optional<StageAdapter<?, T>> getAdapter() {
                return Optional.ofNullable(adapter);
            }

            @Override
            public Executor getExecutor() {
                return executor;
            }

            private Basic(Executor executor, ReferenceIndex<O> old) {
                //noinspection unchecked
                this(executor, old, StageAdapter.map(it -> (T) it));
            }

            private Basic(Executor executor, ReferenceIndex<O> old, StageAdapter<O, T> adapter) {
                this.executor = executor;
                this.refs = old;
                this.adapter = adapter;
            }

            @Override
            public <R> Pipe<T, R> addStage(StageAdapter<T, R> stage) {
                return new Basic<>(executor, this, stage);
            }

            @Override
            public void close() {
                closed = true;
            }

            @Override
            public Pipe<?, T> pipe() {
                return new Basic<>(executor, refs);
            }

            @Override
            public Reference<T> getReference(int index) {
                return Reference.conditional(
                        () -> (index < 0 || refs.size() >= index) && adapter.test(refs.get(index)),
                        () -> adapter.apply(refs.get(index))
                );
            }

            @Override
            public void accept(O it) {
                if (closed)
                    throw new UnsupportedOperationException("Pipe Closed");

                refs.add(it);
            }
        }
    }
}
