package org.comroid.common.func;

import org.comroid.common.ref.Reference;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Cloneable through {@link #process()}.
 */
public interface Processor<T> extends Reference<T>, Cloneable {
    default boolean isPresent() {
        return get() != null;
    }

    static <T> Processor<T> ofReference(Reference<T> reference) {
        return new Support.OfReference<>(reference);
    }

    static <T> Processor<T> empty() {
        //noinspection unchecked
        return (Processor<T>) Support.EMPTY;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> Processor<T> ofOptional(Optional<T> optional) {
        return optional.map(Processor::ofConstant)
                .orElseGet(Processor::empty);
    }

    static <T> Processor<T> ofConstant(T value) {
        return ofReference(Objects.isNull(value) ? Reference.empty() : Reference.constant(value));
    }

    default boolean test(Predicate<? super T> predicate) {
        return predicate.test(get());
    }

    default <R> R into(Function<? super T, R> remapper) {
        return remapper.apply(get());
    }

    @Override
    @Nullable T get();

    @Override
    default Processor<T> process() {
        return Processor.ofReference(this);
    }

    default Processor<T> filter(Predicate<? super T> predicate) {
        return new Support.Filtered<>(this, predicate);
    }

    default <R> Processor<R> map(Function<? super T, ? extends R> mapper) {
        return new Support.Remapped<>(this, mapper);
    }

    default Processor<T> peek(Consumer<? super T> action) {
        return new Support.Remapped<>(this, it -> {
            action.accept(it);
            return it;
        });
    }

    default <R> Stream<R> flatMap(Function<? super T, ? extends Stream<R>> mapper) {
        if (isPresent()) {
            return mapper.apply(get());
        }

        return Stream.empty();
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
        }

        private static final class Remapped<T, R> implements Processor<R> {
            private final Reference<T> underlying;
            private final Function<? super T, ? extends R> remapper;

            private Remapped(Processor<T> base, Function<? super T, ? extends R> remapper) {
                this.underlying = base;
                this.remapper = remapper;
            }

            @Nullable
            @Override
            public R get() {
                return remapper.apply(underlying.get());
            }
        }

        public static class Filtered<T> implements Processor<T> {
            private final Processor<T> underlying;
            private final Predicate<? super T> predicate;

            private Filtered(Processor<T> underlying, Predicate<? super T> predicate) {
                this.underlying = underlying;
                this.predicate = predicate;
            }

            @Nullable
            @Override
            public T get() {
                T result = underlying.get();

                if (predicate.test(result))
                    return result;

                return null;
            }
        }
    }
}
