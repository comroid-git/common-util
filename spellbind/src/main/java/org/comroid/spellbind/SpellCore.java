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
import org.comroid.spellbind.model.MethodInvocation;

import org.jetbrains.annotations.Nullable;

public class SpellCore implements InvocationHandler {
    SpellCore(Object coreObject, Map<String, Invocable<Object>> methodBinds) {
        this.coreObject  = coreObject;
        this.methodBinds = methodBinds;
    }

    @Override
    public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String            methodString = methodString(method);
        final Invocable<Object> invoc        = methodBinds.get(methodString);

        if (invoc instanceof MethodInvocation) {
            MethodInvocation<Object> methodInvocation = (MethodInvocation<Object>) invoc;

            if (Modifier.isAbstract(methodInvocation.getMethod()
                    .getModifiers())) {
                if (!Modifier.isAbstract(method.getModifiers())) {
                    try {
                        invokeDefault(method, args);
                    } catch (IllegalArgumentException | NoSuchElementException e) {
                        throw new InvocationTargetException(e,
                                String.format("Could not invoke method %s: CoreObject is not of its type", methodString)
                        );
                    }
                } else {
                    throw$unimplemented(methodString, null);
                }
            }

            return methodInvocation.invoke(args);
        }

        if (invoc == null) {
            try {
                return invokeDefault(method, args);
            } catch (Throwable e) {
                throw$unimplemented(methodString, e);
            }
        }

        return invoc.invoke(args);
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

    private @Nullable Object invokeDefault(Method method, Object[] args)
            throws IllegalAccessException, InvocationTargetException {
        final Optional<Object> possibleTarget = methodBinds.values()
                .stream()
                .filter(MethodInvocation.class::isInstance)
                .map(MethodInvocation.class::cast)
                .filter(mic -> Spellbind.Builder.findMatchingMethod(method,
                        mic.getTarget()
                                .getClass()
                ) != null)
                .findAny()
                .map(MethodInvocation::getTarget);

        return method.invoke(possibleTarget.orElseThrow(() -> new NoSuchElementException("Could not find a matching target!")),
                args
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
                .map(Class::getSimpleName)
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
    private final Object                         coreObject;
    private final Map<String, Invocable<Object>> methodBinds;
}
