package org.comroid.common.ref;

import org.comroid.common.func.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@FunctionalInterface
public interface Reference<T> extends Supplier<T> {
    static <T> Reference<T> constant(T of) {
        return Objects.isNull(of) ? empty() : new Support.Constant<>(of);
    }

    static <T> Reference<T> empty() {
        return (Reference<T>) Support.EMPTY;
    }

    default boolean isNull() {
        return Objects.isNull(get());
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

    default Processor<T> process() {
        return Processor.ofReference(this);
    }

    interface Settable<T> extends Reference<T> {
        @Nullable T set(T newValue);
    }

    final class Support {
        private static final Reference<?> EMPTY = Reference.constant(null);

        private static final class Constant<T> implements Reference<T> {
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
