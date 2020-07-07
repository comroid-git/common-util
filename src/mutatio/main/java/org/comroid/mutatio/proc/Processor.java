package org.comroid.mutatio.proc;

import org.comroid.api.Polyfill;
import org.comroid.mutatio.ref.Reference;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Cloneable through {@link #process()}.
 */
public interface Processor<T> extends Reference<T>, Cloneable, AutoCloseable {
    default boolean isPresent() {
        return get() != null;
    }

    Optional<? extends Reference<?>> getParent();

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

    default Stream<Reference<?>> upstream() {
        return Stream.concat(
                Stream.of(this),
                getParent()
                        .map(ref -> {
                            if (ref instanceof Processor)
                                return ((Processor<?>) ref).upstream();
                            else return Stream.of(ref);
                        })
                        .orElseGet(Stream::empty)
        );
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

    default Processor<T> or(final Supplier<T> other) {
        return new Support.Or(this, other);
    }

    default Settable<T> snapshot() {
        return upstream()
                .filter(Settable.class::isInstance)
                .findAny()
                .map(Polyfill::<Settable<T>>uncheckedCast)
                .map(ref -> ref.rebind(this))
                .orElseGet(() -> into(Settable::create));
    }

    @Internal
    final class Support {
        private static final Processor<?> EMPTY = new OfReference<>(Reference.empty());

        private static abstract class Abstract<I, O> implements Processor<O> {
            protected final Reference<I> underlying;

            @Override
            public Optional<? extends Reference<?>> getParent() {
                return Optional.ofNullable(underlying);
            }

            protected Abstract(Reference<I> underlying) {
                this.underlying = underlying;
            }
        }

        private static final class OfReference<T> extends Abstract<T, T> {
            private OfReference(Reference<T> underlying) {
                super(underlying);
            }

            @Nullable
            @Override
            public T get() {
                return underlying.get();
            }
        }

        public static final class Remapped<T, R> extends Abstract<T, R> {
            private final Function<? super T, ? extends R> remapper;

            public Remapped(Reference<T> base, Function<? super T, ? extends R> remapper) {
                super(base);

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

        public static final class Filtered<T> extends Abstract<T, T> {
            private final Predicate<? super T> predicate;

            public Filtered(Reference<T> underlying, Predicate<? super T> predicate) {
                super(underlying);

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

        public static final class Or<T> extends Abstract<T, T> {
            private final Supplier<T> other;

            public Or(Reference<T> base, Supplier<T> other) {
                super(base);

                this.other = other;
            }

            @Override
            public T get() {
                return underlying.orElseGet(other);
            }
        }

        private static final class ReferenceFlatMapped<T, R> extends Abstract<T, R> {
            private final Function<? super T, ? extends Reference<R>> mapper;

            public ReferenceFlatMapped(Reference<T> underlying, Function<? super T, ? extends Reference<R>> mapper) {
                super(underlying);

                this.mapper = mapper;
            }

            @Nullable
            @Override
            public R get() {
                return mapper.apply(underlying.get()).get();
            }
        }
    }
}
