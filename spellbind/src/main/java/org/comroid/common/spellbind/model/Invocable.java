package org.comroid.common.spellbind.model;

import org.comroid.common.util.ReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public interface Invocable<T> {
    static <T> Invocable<T> ofConstructor(Constructor<T> constructor) {
        return new Support.OfConstructor<>(constructor);
    }

    static <T> Invocable<T> ofMethodCall(@Nullable Method method) {
        return new Support.OfMethod<>(method, null);
    }

    static <T> Invocable<T> ofMethodCall(Method method, @Nullable Object target) {
        return new Support.OfMethod<>(method, target);
    }

    static <T> Invocable<T> constructing(Class<T> type, Class<?>... args) {
        return ReflectionHelper.findConstructor(type, args)
                .map(Invocable::ofConstructor)
                .orElseThrow(() -> new NoSuchElementException("No suitable constructor found"));
    }

    static <T> Invocable<T> constant(T value) {
        return (Invocable<T>) Support.Constant.Cache.computeIfAbsent(value, Support.Constant::new);
    }

    @Nullable T invoke(Object... args) throws InvocationTargetException, IllegalAccessException;

    Class[] typeOrder();

    default T invokeAutoOrder(Object... args) throws InvocationTargetException, IllegalAccessException {
        return invoke(ReflectionHelper.arrange(args, typeOrder()));
    }

    final class Support {
        private static final class OfConstructor<T> implements Invocable<T> {
            private final Constructor<T> constructor;

            public OfConstructor(Constructor<T> constructor) {
                this.constructor = constructor;
            }

            @Override
            public @Nullable T invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
                try {
                    return constructor.newInstance(args);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Class[] typeOrder() {
                return constructor.getParameterTypes();
            }
        }

        private static final class OfMethod<T> implements Invocable<T> {
            private final Method method;
            private final Object target;

            private OfMethod(Method method, @Nullable Object target) {
                if (target == null && !Modifier.isStatic(method.getModifiers()))
                    throw new IllegalArgumentException("Target cannot be null on non-static methods!",
                            new NullPointerException()
                    );

                this.method = method;
                this.target = target;
            }

            @Nullable
            @Override
            public T invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
                return (T) method.invoke(target, args);
            }

            @Override
            public Class[] typeOrder() {
                return method.getParameterTypes();
            }
        }

        public static class Constant<T> implements Invocable<T> {
            private static final Map<Object, Invocable<Object>> Cache = new ConcurrentHashMap<>();
            private static final Class[]                        NoClasses = new Class[0];
            private final        T                              value;

            public Constant(T value) {
                this.value = value;
            }

            @Nullable
            @Override
            public T invoke(Object... args) {
                return value;
            }

            @Override
            public Class[] typeOrder() {
                return NoClasses;
            }
        }
    }
}
