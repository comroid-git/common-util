package org.comroid.spellbind.model;

import org.comroid.common.ref.Specifiable;
import org.comroid.spellbind.SpellCore;

import java.util.Optional;

public interface TypeFragment<S extends TypeFragment<? super S>> extends Specifiable<S> {
    @Override
    default <R extends S> Optional<R> as(Class<R> type) {
        return SpellCore.findByMember(self()).flatMap(it -> it.getMember(type));
    }
}
