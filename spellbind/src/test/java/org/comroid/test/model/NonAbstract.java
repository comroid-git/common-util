package org.comroid.test.model;

import java.util.Optional;

public interface NonAbstract {
    default Optional<String> string() {
        return cast(String.class);
    }

    default <R> Optional<R> cast(Class<R> to) {
        return to.isInstance(this) ? Optional.of((R) this) : Optional.empty();
    }
}
