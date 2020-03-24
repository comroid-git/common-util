package org.comroid.common.spellbind.model;

import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.Nullable;

public interface Invocable {
    @Nullable Object invoke(Object... args)
            throws InvocationTargetException, IllegalAccessException;
}
