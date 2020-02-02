package org.comroid.common.spellbind.bound;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

public class SpellCore implements InvocationHandler {
    private final Object coreObject;
    private final Map<String, Method> methodBinds;

    private SpellCore(Object coreObject, Map<String, Method> methodBinds) {
        this.coreObject = coreObject;
        this.methodBinds = methodBinds;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodString = methodString(method);
        final Method impl = methodBinds.get(methodString);

        System.out.printf("%s invoked implementation %s; args: %s\n", methodString, methodString(impl), Arrays.toString(args));

        if (impl == null || Modifier.isAbstract(impl.getModifiers()))
            method.invoke(coreObject, args); // TODO: 02.02.2020 We need to keep a Proxy for each Interface for these kinds of calls
            //throw new UnsupportedOperationException(String.format("Method %s has no implementation in this proxy", methodString), new Error(listMethodBinds()));

        return impl.invoke(coreObject, args);
    }

    private String listMethodBinds() {
        final StringBuilder builder = new StringBuilder();

        if (methodBinds.isEmpty())
            return "No MethodBinds registered for " + toString();

        builder.append("All MethodBinds of ").append(toString()).append(":\n")
                .append("\t-\t");

        methodBinds.forEach((key, method) -> builder.append(key).append(" -> ").append(methodString(method)).append("\n\t-\t"));

        return builder.substring(0, builder.length() - 3);
    }

    public static <T> SpellCore forCoreObject(Class<T> mainInterface, T coreObject) {
        final Map<String, Method> methodBinds = new ConcurrentHashMap<>(); // TODO use TrieMap when it's working

        for (Method method : mainInterface.getMethods()) {
            final int mod = method.getModifiers();

            Method implM;
            final boolean anAbstract = Modifier.isAbstract(mod); // is this way better? see invoke(..)
            if (anAbstract && (implM = getMethodImplementation(method, coreObject.getClass())) != null)
                methodBinds.put(methodString(method), implM);
        }

        return new SpellCore(coreObject, methodBinds);
    }

    private static @Nullable Method getMethodImplementation(Method abstractMethod, Class<?> inClass) {
        for (Method method : inClass.getMethods())
            if (abstractMethod.getName().equals(method.getName())
                    && Arrays.equals(abstractMethod.getParameterTypes(), method.getParameterTypes())
                    && abstractMethod.getReturnType().equals(method.getReturnType()))
                return method;

        // TODO: 02.02.2020 doesnt work

        return null;
    }

    private static String methodString(@Nullable Method method) {
        if (method == null)
            return "null";

        return method.toString();
    }

    private static Optional<SpellCore> getInstance(Object ofProxy) {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(ofProxy);

        return invocationHandler instanceof SpellCore ? Optional.of((SpellCore) invocationHandler) : Optional.empty();
    }
}
