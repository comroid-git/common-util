package org.comroid.common.ref;

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
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface Reference<T> extends Supplier<T>, Specifiable<Reference<T>> {
    default boolean isNull() {
        return Objects.isNull(get());
    }

    static <T> Reference<T> constant(T of) {
        return Objects.isNull(of) ? empty() : (Reference<T>) Support.Constant.cache.computeIfAbsent(of, Support.Constant::new);
    }

    static <T> Reference<T> empty() {
        return (Reference<T>) Support.EMPTY;
    }

    static <T> Reference<T> provided(Supplier<T> supplier) {
        return supplier::get;
    }

    static <T> Reference<T> later(CompletableFuture<T> future) {
        return new Support.OfFuture(future);
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
        @Nullable T set(T newValue);

        default T compute(Function<T, T> computor) {
            set(computor.apply(get()));

            return get();
        }
    }

    @Internal
    final class Support {
        private static final Reference<?> EMPTY = Reference.constant(null);

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

        private static final class OfFuture<T> implements Reference<T> {
            private final CompletableFuture<T> future;

            public OfFuture(CompletableFuture<T> future) {
                this.future = future;
            }

            @Nullable
            @Override
            public T get() {
                return future.join();
            }
        }
    }
}
