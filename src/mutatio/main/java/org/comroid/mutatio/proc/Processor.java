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
    Optional<Reference<?>> getParent();

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

    /**
     * Consumes the processor and terminates.
     * Used for completing some calls that return {@linkplain Processor processors}.
     */
    @Override
    default void close() {
        get();
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
        public static abstract class Base<T> extends Reference.Support.Base<T> implements Processor<T> {
            private final Supplier<T> getter;
            private final Function<T, Boolean> setter;

            protected Base(Supplier<T> getter) {
                this(getter, null);
            }

            protected Base(Supplier<T> getter, Function<T, I> converter, Function<I, Boolean> setter) {
                this(getter, converter.andThen(setter));
            }

            protected Base(Supplier<T> getter, @Nullable Function<T, Boolean> setter) {
                super(setter != null);

                this.getter = getter;
                this.setter = setter;
            }

            @Override
            public Optional<Reference<?>> getParent() {
                if (getter instanceof Reference)
                    return Optional.of((Reference<?>) getter);
                return Optional.empty();
            }

            @Override
            protected T doGet() {
                return getter.get();
            }

            @Override
            protected boolean doSet(T value) {
                assert setter != null : "isMutable check wrong";

                return setter.apply(value);
            }
        }

        private static final class Filtered<T> extends Base<T> {
            private final Predicate<? super T> filter;

            public Filtered(Processor<T> base, Predicate<? super T> filter) {
                super(base);

                this.filter = filter;
            }

            @Override
            protected T doGet() {
                final T value = super.doGet();

                if (filter.test(value))
                    return value;
                return null;
            }
        }

        private static final class Remapped<I, O> extends Base<O> {
            public <R> Remapped(Processor<I> base, Function<? super I, ? extends O> remapper) {
                super(base);
            }
        }
    }
}
