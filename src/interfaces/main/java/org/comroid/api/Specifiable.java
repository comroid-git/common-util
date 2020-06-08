package org.comroid.api;

import java.util.Optional;
import java.util.function.Supplier;

public interface Specifiable<B> {
    default <R extends B, T extends Throwable> R as(Class<R> type, Supplier<String> message) throws T {
        return as(type).orElseThrow(() -> new AssertionError(message.get()));
    }

    default <R extends B> Optional<R> as(Class<R> type) {
        if (!isType(type)) {
            return Optional.empty();
        }

        return Optional.of(type.cast(this));
    }

    default boolean isType(Class<? extends B> type) {
        return type.isInstance(this);
    }
}
