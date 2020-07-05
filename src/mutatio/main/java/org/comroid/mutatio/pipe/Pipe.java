package org.comroid.mutatio.pipe;

import org.comroid.api.Disposable;
import org.comroid.api.Polyfill;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.pump.BasicPump;
import org.comroid.mutatio.pump.Pump;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.mutatio.span.Span;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Pipe<I, O> extends ReferenceIndex<O>, Consumer<Reference<I>>, Disposable {
    StageAdapter<I, O> getAdapter();

    default boolean isSorted() {
        return false;
    }

    static <T> Pipe<T, T> create() {
        return new BasicPipe<>(ReferenceIndex.create());
    }

    @SafeVarargs
    static <T> Pipe<T, T> of(T... values) {
        return of(Arrays.asList(values));
    }

    static <T> Pipe<T, T> of(Collection<T> collection) {
        final Pipe<T, T> pipe = create();
        collection.stream()
                .map(Reference::constant)
                .forEach(pipe);

        return pipe;
    }

    @Override
    default List<O> unwrap() {
        return span().unwrap();
    }

    <R> Pipe<O, R> addStage(StageAdapter<O, R> stage);

    default Pipe<O, O> filter(Predicate<? super O> predicate) {
        return addStage(StageAdapter.filter(predicate));
    }

    default <R> Pipe<O, R> map(Function<? super O, ? extends R> mapper) {
        return addStage(StageAdapter.map(mapper));
    }

    default <R> Pipe<O, R> flatMap(Function<? super O, ? extends Reference<? extends R>> mapper) {
        return addStage(StageAdapter.flatMap(mapper));
    }

    default Pipe<O, O> peek(Consumer<? super O> action) {
        return addStage(StageAdapter.peek(action));
    }

    default void forEach(Consumer<? super O> action) {
        addStage(StageAdapter.peek(action)).unwrap();
    }

    default Pipe<O, O> distinct() {
        return addStage(StageAdapter.distinct());
    }

    default Pipe<O, O> limit(long maxSize) {
        return addStage(StageAdapter.limit(maxSize));
    }

    default Pipe<O, O> skip(long skip) {
        return addStage(StageAdapter.skip(skip));
    }

    default Pipe<O, O> sorted() {
        return sorted(Polyfill.uncheckedCast(Comparator.naturalOrder()));
    }

    default Pipe<O, O> sorted(Comparator<? super O> comparator) {
        return new SortedResultingPipe<>(this, comparator);
    }

    @NotNull
    default Processor<O> findFirst() {
        return sorted().findAny();
    }

    @NotNull
    default Processor<O> findAny() {
        return Processor.ofReference(Reference.conditional(() -> size() > 0, () -> get(0)));
    }

    @Override
    default boolean remove(O item) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("remove() is not supported by pipe");
    }

    @Override
    default Pump<I, O> pump(Executor executor) {
        return new BasicPump<>(executor, this.map(Polyfill::uncheckedCast));
    }

    <X> BiPipe<O, X, O, X> bi(Function<O, X> mapper);

    /**
     * Only meant for use from a {@link Pump} instance.
     */
    @Override
    default void accept(Reference<I> input) {
        throw new UnsupportedOperationException("Method #accept is only meant for use from a Pump instance");
    }

    default Span<O> span() {
        return new Span<>(this, Span.DefaultModifyPolicy.SKIP_NULLS);
    }

    default CompletableFuture<O> next() {
        class OnceCompletingStage implements StageAdapter<O, O> {
            private final CompletableFuture<O> future = new CompletableFuture<>();

            @Override
            public Reference<O> advance(Reference<O> ref) {
                if (!ref.isNull() && !future.isDone())
                    future.complete(ref.get());
                return Reference.empty();
            }
        }

        final OnceCompletingStage stage = new OnceCompletingStage();
        final Pipe<O, O> resulting = addStage(stage);
        stage.future.thenRun(resulting::close);

        return stage.future;
    }
}
