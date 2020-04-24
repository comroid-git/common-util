package org.comroid.spellbind.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.comroid.common.func.Invocable;

import org.jetbrains.annotations.Nullable;

public final class MethodInvocation<T> implements Invocable<T> {
    public MethodInvocation(Method method, Object target) {
        this(method, target, null);
    }

    public MethodInvocation(Method method, Object target, Object[] defaultArgs) {
        this.method = method;
        this.target = target;
        this.args   = defaultArgs;
    }

    public Method getMethod() {
        return method;
    }

    public Object getTarget() {
        return target;
    }

    public Object[] getDefaultArgs() {
        return args;
    }

    @Nullable
    @Override
    public T invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        //noinspection unchecked
        return (T) method.invoke(target, args == null && this.args != null ? this.args : args);
    }

    @Override
    public Class<?>[] typeOrder() {
        return method.getParameterTypes();
    }
    private final Method   method;
    private final Object   target;
    private final Object[] args;
}
