package org.comroid.spellbind;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.common.ref.SelfDeclared;
import org.comroid.common.ref.Specifiable;
import org.comroid.common.util.ArrayUtil;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.spellbind.model.TypeFragment;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpellCore<T extends TypeFragment<? super T>> implements InvocationHandler {
    private final Spellbind.ReproxyFragment<T> reproxy;
    private final Map<String, Invocable<Object>> methodBinds;

    SpellCore(
            Spellbind.ReproxyFragment<T> reproxy,
            Map<String, Invocable<Object>> methodBinds
    ) {
        this.reproxy = reproxy;
        this.methodBinds = methodBinds;
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

    private static <T extends TypeFragment<? super T>> Optional<SpellCore<T>> getInstance(T ofProxy) {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(ofProxy);

        return invocationHandler instanceof SpellCore
                ? Optional.of(Polyfill.uncheckedCast(invocationHandler))
                : Optional.empty();
    }

    @Override
    public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodString = methodString(method);
        final Invocable<Object> invoc = methodBinds.get(methodString);

        if ((method.getDeclaringClass().equals(Specifiable.class)
                || method.getDeclaringClass().equals(SelfDeclared.class))
                && invoc == null) {
            switch (method.getName()) {
                case "as":
                    if (args.length != 1) break;


                    break;
            }
            return method.invoke(reproxy.self(), ReflectionHelper.arrange(args, method.getParameterTypes()));
        }

        if (invoc == null)
            throw$unimplemented(methodString, new NoSuchElementException("Bound Invocable"));

        return invoc.autoInvoke(ArrayUtil.insert(args, args.length, this));
    }

    private void throw$unimplemented(Object methodString, @Nullable Throwable e) throws UnsupportedOperationException {
        throw e == null ? new UnsupportedOperationException(String.format("Method %s has no implementation in this proxy",
                methodString
        )) : new UnsupportedOperationException(String.format("Method %s has no " + "implementation in this proxy", methodString),
                e
        );
    }
}
