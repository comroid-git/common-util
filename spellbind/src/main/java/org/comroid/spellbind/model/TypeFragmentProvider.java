package org.comroid.spellbind.model;

import org.comroid.common.func.Invocable;
import org.comroid.spellbind.Spellbind;

import java.util.Objects;
import java.util.function.BiConsumer;

public interface TypeFragmentProvider<T extends TypeFragment> extends BiConsumer<Spellbind.Builder<?>, Object[]> {
    Class<T> getInterface();

    Invocable.TypeMap<? extends T> getInstanceSupplier();

    @Override
    default void accept(Spellbind.Builder<?> builder, Object... args) {
        builder.subImplement(
                Objects.requireNonNull(getInstanceSupplier().autoInvoke(args), "Could not construct TypeFragment"),
                getInterface()
        );
    }
}
