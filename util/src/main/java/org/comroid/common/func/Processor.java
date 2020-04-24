package org.comroid.common.func;

import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.comroid.common.ref.Reference;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * Cloneable through {@link #process()}.
 */
public interface Processor<T> extends Reference<T>, Cloneable {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> Processor<T> ofOptional(Optional<T> optional) {
        return optional.map(Processor::ofConstant)
                .orElseGet(Processor::empty);
    }

    static <T> Processor<T> ofConstant(T value) {
        return ofReference(Objects.isNull(value) ? Reference.empty() : Reference.constant(value));
    }

    static <T> Processor<T> empty() {
        //noinspection unchecked
        return (Processor<T>) Support.EMPTY;
    }
  
    static <T> Processor<T> ofReference(Reference<T> reference) {
        return new Support.OfReference<>(reference);
    }

    @Internal
    final class Support {
        private static final Processor<?> EMPTY = new OfReference<>(Reference.empty());

        private static final class OfReference<T> implements Processor<T> {
            private final Reference<T> underlying;

            private OfReference(Reference<T> underlying) {
                this.underlying = underlying;
            }

            @Nullable
            @Override
            public T get() {
                return underlying.get();
            }

            @Override
            public boolean isPresent() {
                return !underlying.isNull();
            }
        }

        private static final class Remapped<T, R> implements Processor<R> {
            private final Processor<T>                     base;
            private final Function<? super T, ? extends R> remapper;

            private Remapped(Processor<T> base, Function<? super T, ? extends R> remapper) {
                this.base     = base;
                this.remapper = remapper;
            }

            @Nullable
            @Override
            public R get() {
                return remapper.apply(base.get());
            }

            @Override
            public boolean isPresent() {
                return base.isPresent();
            }
        }
    }

    default boolean test(Predicate<? super T> predicate) {
        return predicate.test(get());
    }

    @Override
    @Nullable T get();

    @Override
    default Processor<T> process() {
        return Processor.ofReference(this);
    }

    default Processor<T> filter(Predicate<? super T> predicate) {
        if (isPresent() && predicate.test(get())) {
            return this;
        }

        return empty();
    }

    boolean isPresent();

    static <T> Processor<T> empty() {
        return (Processor<T>) Support.EMPTY;
    }

    default <R> Processor<R> map(Function<? super T, ? extends R> mapper) {
        if (isPresent()) {
            return new Support.Remapped<>(this, mapper);
        }

        return empty();
    }

    default <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        if (isPresent()) {
            return StreamSupport.stream(Spliterators.spliterator(new Object[]{ mapper.apply(get()) },
                    Spliterator.SIZED
            ), false);
        }

        return Stream.empty();
    }

    default Processor<T> peek(Consumer<? super T> action) {
        if (isPresent()) {
            action.accept(get());
        }

        return this;
    }

    default boolean test(Predicate<? super T> predicate) {
        return predicate.test(get());
    }

    @Override
    @Nullable T get();

    @Override
    default Processor<T> process() {
        return Processor.ofReference(this);
    }

    default Processor<T> filter(Predicate<? super T> predicate) {
        if (isPresent() && predicate.test(get())) {
            return this;
        }

        return empty();
    }

    boolean isPresent();

    default <R> Processor<R> map(Function<? super T, ? extends R> mapper) {
        if (isPresent()) {
            return new Support.Remapped<>(this, mapper);
        }

        return empty();
    }

    default <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        if (isPresent()) {
            return StreamSupport.stream(Spliterators.spliterator(new Object[]{ mapper.apply(get()) }, Spliterator.SIZED), false);
        }

        return Stream.empty();
    }

    default Processor<T> peek(Consumer<? super T> action) {
        if (isPresent()) {
            action.accept(get());
        }

        return this;
    }
}
