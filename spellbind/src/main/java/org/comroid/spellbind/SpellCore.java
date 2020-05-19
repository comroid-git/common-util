package org.comroid.spellbind;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.comroid.common.func.Invocable;

import org.jetbrains.annotations.Nullable;

public class SpellCore implements InvocationHandler {
    private final Object                         coreObject;
    private final Map<String, Invocable<Object>> methodBinds;

    SpellCore(Object coreObject, Map<String, Invocable<Object>> methodBinds) {
        this.coreObject  = coreObject;
        this.methodBinds = methodBinds;
    }

    @Override
    public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String            methodString = methodString(method);
        final Invocable<Object> invoc        = methodBinds.get(methodString);

        if (invoc == null)
            throw$unimplemented(methodString, new NoSuchElementException("Bound Invocable"));

        try {
            return invoc.invoke(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String methodString(@Nullable Method method) {
        if (method == null) {
            return "null";
        }

        return String.format("%s#%s(%s)%s: %s",
                method.getDeclaringClass()
                        .getName(),
                method.getName(),
                paramString(method),
                throwsString(method),
                method.getReturnType()
                        .getSimpleName()
        );
    }

    private void throw$unimplemented(Object methodString, @Nullable Throwable e) throws UnsupportedOperationException {
        throw e == null ? new UnsupportedOperationException(String.format("Method %s has no implementation in this proxy",
                methodString
        )) : new UnsupportedOperationException(String.format("Method %s has no " + "implementation in this proxy", methodString),
                e
        );
    }

    private static String paramString(Method method) {
        return Stream.of(method.getParameterTypes())
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    private static String throwsString(Method method) {
        final Class<?>[] exceptionTypes = method.getExceptionTypes();

        return exceptionTypes.length == 0
                ? ""
                : Stream.of(exceptionTypes)
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", ", " throws ", ""));
    }

    private static Optional<SpellCore> getInstance(Object ofProxy) {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(ofProxy);

        return invocationHandler instanceof SpellCore ? Optional.of((SpellCore) invocationHandler) : Optional.empty();
    }
}
