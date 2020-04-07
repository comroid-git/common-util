package org.comroid.common.ref;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Reference<T> extends Supplier<T> {
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
}
