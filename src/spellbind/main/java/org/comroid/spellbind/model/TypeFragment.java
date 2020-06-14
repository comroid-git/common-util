package org.comroid.spellbind.model;

import org.comroid.api.SelfDeclared;

import java.util.UUID;

public interface TypeFragment<S extends TypeFragment<? super S>> extends SelfDeclared<S> {
    UUID getUUID();

    @Override
    default S self() {
        return null; //Todo
    }
}
