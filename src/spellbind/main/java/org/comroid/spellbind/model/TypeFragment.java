package org.comroid.spellbind.model;

import org.comroid.api.Polyfill;
import org.comroid.api.SelfDeclared;
import org.comroid.common.exception.AssertionException;
import org.comroid.spellbind.SpellCore;

import java.util.UUID;

public interface TypeFragment<S extends TypeFragment<? super S>> extends SelfDeclared<S> {
    UUID getUUID();

    @Override
    default S self() {
        return SpellCore.getCore(this)
                .map(Polyfill::<S>uncheckedCast)
                .orElseThrow(AssertionException::new);
    }
}
