package org.comroid.spellbind;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.common.util.ArrayUtil;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.spellbind.annotation.Partial;
import org.comroid.spellbind.model.TypeFragment;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SpellCore<T extends TypeFragment<? super T>> implements InvocationHandler {
    private static final Collection<SpellCore<?>> instances = new ArrayList<>();
    private final Spellbind.ReproxyFragment<T> reproxy;
    private final Map<String, Invocable<Object>> methodBinds;
    private final Map<Class<?>, Object> members;

    SpellCore(
            Spellbind.ReproxyFragment<T> reproxy,
            Map<String, Invocable<Object>> methodBinds,
            Map<Class<?>, Object> members
    ) {
        this.reproxy = reproxy;
        this.methodBinds = methodBinds;
        this.members = Collections.unmodifiableMap(members);

        instances.add(this);
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

    public static <T extends TypeFragment<? super T>> Optional<SpellCore<T>> findByMember(T member) {
        return instances.stream()
                .filter(it -> it.members.containsKey(findPartialClass(member.getClass())
                        .orElseThrow(() -> new NoSuchElementException(String
                                .format("No Partial class could be found for member %s of type %s",
                                        member, member.getClass())))))
                .findFirst()
                .map(Polyfill::uncheckedCast);
    }

    public static Optional<Class<?>> findPartialClass(Class<? extends TypeFragment> in) {
        return StreamSupport.stream(ReflectionHelper.recursiveClassGenerator(in), false)
                .filter(type -> type.isAnnotationPresent(Partial.class))
                .findFirst();
    }

    public <E extends T> Optional<E> getMember(Class<E> type) {
        return Optional.ofNullable(this.members.get(type))
                .map(Polyfill::uncheckedCast);
    }

    @Override
    public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodString = methodString(method);
        final Invocable<Object> invoc = methodBinds.get(methodString);

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
