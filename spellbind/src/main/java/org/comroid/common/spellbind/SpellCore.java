package org.comroid.common.spellbind;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.comroid.common.spellbind.model.Invocation;

import org.jetbrains.annotations.Nullable;

public class SpellCore implements InvocationHandler {
    private final Object coreObject;
    private final Map<String, Invocation> methodBinds;

    SpellCore(Object coreObject, Map<String, Invocation> methodBinds) {
        this.coreObject = coreObject;
        this.methodBinds = methodBinds;
    }

    @Override
    public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodString = methodString(method);
        final Invocation impl = methodBinds.get(methodString);

        if (impl == null || Modifier.isAbstract(impl.method.getModifiers()))
            if (!Modifier.isAbstract(method.getModifiers()))
                method.invoke(args);
            else throw$unimplemented(methodString);

        if (impl == null)
            throw$unimplemented(methodString);

        return impl.invoke(args);
    }

    private void throw$unimplemented(Object methodString) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(String.format("Method %s has no implementation in this proxy", methodString));
    }

    public static String methodString(@Nullable Method method) {
        if (method == null)
            return "null";

        return String.format("%s#%s(%s)%s: %s", method.getDeclaringClass().getName(), method.getName(), paramString(method), throwsString(method), method.getReturnType().getSimpleName());
    }

    private static String paramString(Method method) {
        return Stream.of(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
    }

    private static String throwsString(Method method) {
        final Class<?>[] exceptionTypes = method.getExceptionTypes();

        return exceptionTypes.length == 0 ? "" : Stream.of(exceptionTypes)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ", " throws ", ""));
    }

    private static Optional<SpellCore> getInstance(Object ofProxy) {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(ofProxy);

        return invocationHandler instanceof SpellCore ? Optional.of((SpellCore) invocationHandler) : Optional.empty();
    }
}
