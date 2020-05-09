package org.comroid.spellbind.model;

import org.comroid.common.func.Invocable;
import org.comroid.common.ref.Pair;
import org.comroid.common.ref.Reference;

import java.lang.reflect.Modifier;

public final class SubImplementation<T> extends Pair<Class<T>, Invocable<? extends T>> {
    public SubImplementation(Class<T> targetInterface, Invocable<? extends T> instanceSupplier) throws IllegalArgumentException {
        super(targetInterface, instanceSupplier);

        if (!Modifier.isInterface(targetInterface.getModifiers()))
            throw new IllegalArgumentException("Target class is not an interface: " + targetInterface.getName());
    }

    public Class<T> getTargetInterface() {
        return super.getFirst();
    }

    public Invocable<? extends T> getInstanceSupplier() {
        return super.getSecond();
    }
}
