package org.comroid.spellbind.model;

import org.comroid.api.Invocable;

import java.util.function.BiConsumer;

public interface TypeFragmentProvider<T extends TypeFragment<T>> extends BiConsumer<Spellbind.Builder<?>, Object[]> {
    Class<T> getInterface();

    Invocable.TypeMap<? extends T> getInstanceSupplier();

    @Override
    default void accept(Spellbind.Builder<?> builder, Object... args) {
        builder.subImplement(getInstanceSupplier().autoInvoke(args), getInterface());
    }
}
