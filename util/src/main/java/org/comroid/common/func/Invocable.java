package org.comroid.common.func;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.comroid.common.util.ReflectionHelper;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Invocable<T> {
    static <T> Invocable<T> ofProvider(Provider<T> provider) {
        return new Support.OfProvider<>(provider);
    }

    static <T> Invocable<T> ofConsumer(Class<T> type, Consumer<T> consumer) {
        return new Support.OfConsumer<T>(type, consumer);
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

    static <T> Invocable<T> ofConstructor(Constructor<T> constructor) {
        return new Support.OfConstructor<>(constructor);
    }

    static <T> Invocable<T> constant(T value) {
        //noinspection unchecked
        return (Invocable<T>) Support.Constant.Cache.computeIfAbsent(value, Support.Constant::new);
    }

    static <T> Invocable<T> empty() {
        //noinspection unchecked
        return (Invocable<T>) Support.Empty;
    }

    @Internal
    final class Support {
        private static final Invocable<?> Empty     = constant(null);
        private static final Class<?>[]   NoClasses = new Class[0];

        private static final class OfProvider<T> implements Invocable<T> {
            private final Provider<T> provider;

            public OfProvider(Provider<T> provider) {
                this.provider = provider;
            }

            @Nullable
            @Override
            public T invoke(Object... args) {
                return provider.now();
            }

            @Override
            public Class<?>[] typeOrder() {
                return NoClasses;
            }
        }

        private static final class OfConstructor<T> implements Invocable<T> {
            private final Constructor<T> constructor;

            public OfConstructor(Constructor<T> constructor) {
                this.constructor = constructor;
            }

            @Override
            public @NotNull T invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
                try {
                    return constructor.newInstance(args);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Class<?>[] typeOrder() {
                return constructor.getParameterTypes();
            }
        }

        private static final class OfMethod<T> implements Invocable<T> {
            private final Method method;
            private final Object target;

            private OfMethod(Method method, @Nullable Object target) {
                if (target == null && !Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException("Target cannot be null on non-static methods!",
                            new NullPointerException()
                    );
                }

                this.method = method;
                this.target = target;
            }

            @Nullable
            @Override
            public T invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
                //noinspection unchecked
                return (T) method.invoke(target, args);
            }

            @Override
            public Class<?>[] typeOrder() {
                return method.getParameterTypes();
            }
        }

        private static final class Constant<T> implements Invocable<T> {
            private static final Map<Object, Invocable<Object>> Cache = new ConcurrentHashMap<>();
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
            public Class<?>[] typeOrder() {
                return NoClasses;
            }
        }

        private static final class OfConsumer<T> implements Invocable<T> {
            private final Class<T>    argType;
            private final Consumer<T> consumer;
            private final Class<?>[]  argTypeArr;

            public OfConsumer(Class<T> argType, Consumer<T> consumer) {
                this.argType    = argType;
                this.consumer   = consumer;
                this.argTypeArr = new Class[]{ argType };
            }

            @Nullable
            @Override
            public T invoke(Object... args) {
                if (argType.isInstance(args[0])) {
                    consumer.accept(argType.cast(args[0]));
                    return null;
                } else {
                    throw new IllegalArgumentException(String.format("Invalid Type: %s",
                            args[0].getClass()
                                    .getName()
                    ));
                }
            }

            @Override
            public Class<?>[] typeOrder() {
                return argTypeArr;
            }
        }
    }

    default T invokeAutoOrder(Object... args) throws InvocationTargetException, IllegalAccessException {
        return invoke(ReflectionHelper.arrange(args, typeOrder()));
    }

    @Nullable T invoke(Object... args) throws InvocationTargetException, IllegalAccessException;

    Class<?>[] typeOrder();

    default void invokeRethrow(Object... args) {
        try {
            invoke(args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
