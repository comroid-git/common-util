package org.comroid.spellbind.model;

import org.comroid.api.Invocable;

import java.util.function.BiConsumer;

public interface TypeFragmentProvider<T extends TypeFragment<? super T>> {
    Class<T> getInterface();

    Invocable<? extends T> getInstanceSupplier();
}
