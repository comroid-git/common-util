package org.comroid.common.ref;

import org.comroid.common.func.Processor;
import org.comroid.common.func.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@FunctionalInterface
public interface Reference<T> extends Supplier<T> {
    static <T> Reference<T> constant(T of) {
        return Objects.isNull(of) ? empty() : (Reference<T>) Support.Constant.cache.computeIfAbsent(of,
                Support.Constant::new
        );
    }

    static <T> Reference<T> empty() {
        return (Reference<T>) Support.EMPTY;
    }

    @Override
    @Nullable T get();

    default boolean isNull() {
        return Objects.isNull(get());
    }

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

    default Processor<T> process() {
        return Processor.ofReference(this);
    }

    interface Settable<T> extends Reference<T> {
        @Nullable T set(T newValue);
    }

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
    }
}
