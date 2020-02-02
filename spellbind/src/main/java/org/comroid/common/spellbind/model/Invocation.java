package org.comroid.common.spellbind.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jetbrains.annotations.Nullable;

public class Invocation {
    public final Object target;
    public final Method method;

    public Invocation(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    public @Nullable Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return invoke(target, args);
    }

    public @Nullable Object invoke(Object target, Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }
}
