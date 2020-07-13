package org.comroid.mutatio.ref;

import org.comroid.api.Invocable;
import org.comroid.api.Polyfill;
import org.comroid.api.Provider;
import org.comroid.api.Specifiable;
import org.comroid.mutatio.cache.CachedValue;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.proc.Processor;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Stream;

public interface Reference<T> extends CachedValue<T>, Supplier<T>, Specifiable<Reference<T>> {
    default boolean isNull() {
        return test(Objects::isNull);
    }

    default boolean isPresent() {
        return test(Objects::nonNull);
    }

    static <T> Reference<T> constant(@Nullable T of) {
        //noinspection unchecked
        return Objects.isNull(of)
                ? empty()
                : (Reference<T>) Support.Constant.cache.computeIfAbsent(of, Support.Constant::new);
    }

    static <T> Reference<T> empty() {
        //noinspection unchecked
        return (Reference<T>) Support.EMPTY;
    }

    static <T> Reference<T> provided(Supplier<T> supplier) {
        return new Support.Conditional<>(() -> true, supplier);
    }

    static <T> Reference<T> conditional(BooleanSupplier condition, Supplier<T> supplier) {
        return new Support.Conditional<>(condition, supplier);
    }

    static <T> FutureReference<T> later(CompletableFuture<T> future) {
        return new FutureReference<>(future);
    }

    @Override
    @Nullable T get();

    default Optional<T> wrap() {
        return Optional.ofNullable(get());
    }

    default Stream<T> stream() {
        if (isNull())
            return Stream.empty();
        return Stream.of(get());
    }

    default @NotNull T requireNonNull() throws NullPointerException {
        return Objects.requireNonNull(get());
    }

    default @NotNull T requireNonNull(String message) throws NullPointerException {
        return Objects.requireNonNull(get(), message);
    }

    default @NotNull T orElse(T other) {
        if (isNull())
            return other;
        return requireNonNull("Assertion Failure");
    }

    default @NotNull T orElseGet(Supplier<T> otherProvider) {
        if (isNull())
            return otherProvider.get();
        return requireNonNull("Assertion Failure");
    }

    default Provider<T> provider() {
        return Provider.of(this);
    }

    default Invocable<T> invocable() {
        return Invocable.ofProvider(Provider.of(this));
    }

    default Processor<T> process() {
        return Processor.ofReference(this);
    }

    default Pipe<T, T> pipe() {
        return Pipe.of(get());
    }

    default boolean test(Predicate<? super T> predicate) {
        return predicate.test(get());
    }

    default <R> R into(Function<? super T, R> remapper) {
        return remapper.apply(get());
    }

    default boolean contentEquals(T other) {
        if (other == null)
            return isNull();
        return into(other::equals);
    }

    default void consume(Consumer<T> consumer) {
        consumer.accept(get());
    }

    default void ifPresent(Consumer<T> consumer) {
        if (isPresent())
            consume(consumer);
    }

    default void ifEmpty(Runnable task) {
        if (isNull())
            task.run();
    }

    default void ifPresentOrElse(Consumer<T> consumer, Runnable task) {
        if (isPresent())
            consume(consumer);
        else task.run();
    }

    default <R> @Nullable R ifPresentMap(Function<T, R> consumer) {
        if (isPresent())
            return into(consumer);
        return null;
    }

    default <R> R ifPresentMapOrElse(Function<T, R> consumer, Supplier<R> task) {
        if (isPresent())
            return into(consumer);
        else return task.get();
    }

    interface Settable<T> extends Reference<T> {

        static <T> Settable<T> create() {
            return create(null);
        }

        static <T> Settable<T> create(@Nullable T initialValue) {
            return new Support.Settable<>(initialValue);
        }

        /**
         * @return The new value if it could be set, else the previous value.
         */
        @Nullable T set(T newValue);

        /**
         * @return The new value if it could be set, else the previous value.
         */
        default T compute(Function<T, T> computor) {
            set(computor.apply(get()));

            return get();
        }

        /**
         * @return The new value if it could be set, else the previous value.
         */
        default T computeIfPresent(Function<T, T> computor) {
            if (!isNull())
                set(computor.apply(get()));

            return get();
        }

        /**
         * @return The new value if it could be set, else the previous value.
         */
        default T computeIfAbsent(Supplier<T> supplier) {
            if (isNull())
                set(supplier.get());

            return get();
        }

        default Settable<T> rebind(Supplier<T> behind) {
            class Rebound implements Settable<T> {
                private final Settable<T> setter;
                private final Supplier<T> getter;

                public Rebound(Settable<T> setter, Supplier<T> getter) {
                    this.setter = setter;
                    this.getter = getter;
                }

                @Nullable
                @Override
                public T get() {
                    return getter.get();
                }

                @Nullable
                @Override
                public T set(T newValue) {
                    return setter.set(newValue);
                }
            }

            return new Rebound(this, behind);
        }
    }

    @Internal
    final class Support {
        private static final Reference<?> EMPTY = new Constant<>(null);

        private static final class Constant<T> extends CachedValue.Abstract<T> implements Reference<T> {
            private static final Map<Object, Constant<Object>> cache = new ConcurrentHashMap<>();
            private final T value;

            private Constant(T value) {
                this.value = value;
            }

            @Nullable
            @Override
            public T get() {
                return value;
            }
        }

        private static final class Conditional<T> implements Reference<T> {
            private final BooleanSupplier condition;
            private final Supplier<T> supplier;

            private Conditional(BooleanSupplier condition, Supplier<T> supplier) {
                this.condition = condition;
                this.supplier = supplier;
            }

            @Nullable
            @Override
            public T get() {
                //noinspection unchecked
                return condition.getAsBoolean() ? supplier.get() : (T) empty().get();
            }
        }

        private static final class Settable<T> implements Reference.Settable<T> {
            private final Object lock = Polyfill.selfawareLock();
            private @Nullable T value;

            public Settable() {
                this(null);
            }

            public Settable(@Nullable T initialValue) {
                this.value = initialValue;
            }

            @Nullable
            @Override
            public T get() {
                synchronized (lock) {
                    return value;
                }
            }

            @Nullable
            @Override
            public T set(T newValue) {
                synchronized (lock) {
                    return (this.value = newValue);
                }
            }
        }
    }
}
