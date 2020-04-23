package org.comroid.common.ref;

import java.util.Optional;

import org.comroid.common.info.MessageSupplier;

public interface Specifiable<B> {
    default <R extends B, T extends Throwable> R as(Class<R> type, MessageSupplier message)
            throws T {
        return as(type).orElseThrow(() -> new AssertionError(message));
    }

    default <R extends B> Optional<R> as(Class<R> type) {
        if (!isType(type)) return Optional.empty();

        return Optional.of(type.cast(this));
    }

    default boolean isType(Class<? extends B> type) {
        return type.isInstance(this);
    }
}
