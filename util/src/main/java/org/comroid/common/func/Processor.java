package org.comroid.common.func;

import org.comroid.common.ref.Reference;
import org.jetbrains.annotations.Nullable;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Cloneable through {@link #process()}.
 * 
 * @param <T>
 */
public interface Processor<T> extends Reference<T>, Cloneable {
    static <T> Processor<T> empty() {
        return (Processor<T>) Support.EMPTY;
    }

    static <T> Processor<T> ofConstant(T value) {
        return ofReference(Reference.constant(value));
    }

    static <T> Processor<T> ofReference(Reference<T> reference) {
        return new Support.OfReference<>(reference);
    }

    boolean isPresent();

    @Override
    @Nullable T get();

    @Override
    default Processor<T> process() {
        return Processor.ofReference(this);
    }

    default boolean test(Predicate<? super T> predicate) {
        return predicate.test(get());
    }

    default Processor<T> filter(Predicate<? super T> predicate) {
        if (isPresent() && predicate.test(get()))
            return this;

        return empty();
    }

    default <R> Processor<R> map(Function<? super T, ? extends R> mapper) {
        if (isPresent())
            return new Support.Remapped<>(this, mapper);

        return empty();
    }

    default <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        if (isPresent())
            return StreamSupport.stream(Spliterators.spliterator(new Object[]{mapper.apply(get())}, Spliterator.SIZED),
                    false
            );

        return Stream.empty();
    }

    default Processor<T> peek(Consumer<? super T> action) {
        if (isPresent())
            action.accept(get());

        return this;
    }

    final class Support {
        private static final Processor<?> EMPTY = new OfReference<>(Reference.empty());

        private static final class OfReference<T> implements Processor<T> {
            private final Reference<T> underlying;

            private OfReference(Reference<T> underlying) {
                this.underlying = underlying;
            }

            @Override
            public boolean isPresent() {
                return !underlying.isNull();
            }

            @Nullable
            @Override
            public T get() {
                return underlying.get();
            }
        }

        private static final class Remapped<T, R> implements Processor<R> {
            private final Processor<T>                     base;
            private final Function<? super T, ? extends R> remapper;

            private Remapped(Processor<T> base, Function<? super T, ? extends R> remapper) {
                this.base     = base;
                this.remapper = remapper;
            }

            @Override
            public boolean isPresent() {
                return base.isPresent();
            }

            @Nullable
            @Override
            public R get() {
                return remapper.apply(base.get());
            }
        }
    }
}
