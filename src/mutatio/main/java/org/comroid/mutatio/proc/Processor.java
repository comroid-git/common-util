package org.comroid.mutatio.proc;

import org.comroid.mutatio.ref.Reference;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Cloneable through {@link #process()}.
 */
public interface Processor<T> extends Reference<T>, Cloneable, AutoCloseable {
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

    static <T> Processor<T> providedOptional(Supplier<Optional<T>> supplier) {
        return new Support.OfReference<>(Reference.provided(() -> supplier.get().orElse(null)));
    }

    @Override
    @Nullable T get();

    /**
     * Consumes the processor and terminates.
     * Used for completing some calls that return {@linkplain Processor processors}.
     */
    @Override
    default void close() {
        get();
    }

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

    default <R> Processor<R> flatMap(Function<? super T, ? extends Reference<R>> mapper) {
        return new Support.ReferenceFlatMapped<>(this, mapper);
    }

    default <R> Processor<R> flatMapOptional(Function<? super T, ? extends Optional<R>> mapper) {
        return flatMap(mapper
                .andThen(Optional::get)
                .andThen(Reference::constant));
    }

    default Settable<T> snapshot() {
        return Settable.create(get());
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
                final T get = underlying.get();

                if (get != null)
                    return remapper.apply(get);

                return null;
            }
        }

        private static final class Filtered<T> implements Processor<T> {
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

        private static final class ReferenceFlatMapped<T, R> implements Processor<R> {
            private final Reference<T> base;
            private final Function<? super T, ? extends Reference<R>> mapper;

            public ReferenceFlatMapped(Reference<T> base, Function<? super T, ? extends Reference<R>> mapper) {
                this.base = base;
                this.mapper = mapper;
            }

            @Nullable
            @Override
            public R get() {
                return mapper.apply(base.get()).get();
            }
        }
    }
}
