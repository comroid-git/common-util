package org.comroid.common.spellbind.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jetbrains.annotations.Nullable;

public class MethodInvocation implements Invocable {
    public final Object target;
    public final Method method;

    public MethodInvocation(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    @Override
    public @Nullable Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }
}
