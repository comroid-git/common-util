package de.kaleidox.spellbind.test.model;

import java.util.Optional;

public interface NonAbstract {
    default <R> Optional<R> cast(Class<R> to) {
        return to.isInstance(this) ? Optional.of((R) this) : Optional.empty();
    }

    default Optional<String> string() {
        return cast(String.class);
    }
}
