package org.comroid.common.ref;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.common.func.Processor;
import org.comroid.common.func.Provider;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface Reference<T> extends Supplier<T>, Specifiable<Reference<T>> {
    default boolean isNull() {
        return Objects.isNull(get());
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
        return supplier::get;
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

    default @NotNull T requireNonNull() throws NullPointerException {
        return Objects.requireNonNull(get());
    }

    default @NotNull T requireNonNull(String message) throws NullPointerException {
        return Objects.requireNonNull(get(), message);
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

    interface Settable<T> extends Reference<T> {
        static <T> Settable<T> create() {
            return create(null);
        }

        static <T> Settable<T> create(T initialValue) {
            return new Support.Settable<>(initialValue);
        }

        @Nullable T set(T newValue);

        default T compute(Function<T, T> computor) {
            set(computor.apply(get()));

            return get();
        }
    }

    @Internal
    final class Support {
        private static final Reference<?> EMPTY = new Constant<>(null);

        private static final class Constant<T> implements Reference<T> {
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
                    return value;
                }
            }
        }
    }
}
