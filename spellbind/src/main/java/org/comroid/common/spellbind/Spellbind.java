package org.comroid.common.spellbind;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.comroid.common.func.Invocable;
import org.comroid.common.map.TrieMap;

import org.jetbrains.annotations.Nullable;

import static org.comroid.common.spellbind.SpellCore.methodString;

public final class Spellbind {
    public static <T> Builder<T> builder(Class<T> mainInterface) {
        return new Builder<>(mainInterface);
    }

    public static class Builder<T> implements org.comroid.common.func.Builder<T> {
        private final Class<T> mainInterface;
        private final Map<String, Invocable> methodBinds;
        private final Collection<Class<?>> interfaces;
        private       Object coreObject;
        private       ClassLoader classLoader;

        public Builder(Class<T> mainInterface) {
            this.mainInterface = mainInterface;
            this.methodBinds   = TrieMap.ofString();
            this.interfaces    = new ArrayList<>(1);

            interfaces.add(mainInterface);
        }

        @Override
        public T build() {
            final ClassLoader classLoader = Optional.ofNullable(this.classLoader)
                    .orElseGet(Spellbind.class::getClassLoader);

            final SpellCore spellCore = new SpellCore(coreObject, methodBinds);

            return (T) Proxy.newProxyInstance(classLoader,
                    interfaces.stream()
                            .distinct()
                            .toArray(Class[]::new),
                    spellCore
            );
        }

        public Builder<T> coreObject(Object coreObject) {
            this.coreObject = coreObject;

            final Class<?> coreObjectClass = coreObject.getClass();

            populateBinds(mainInterface.getMethods(), coreObject, methodBinds);

            return this;
        }

        private void populateBinds(
                Method[] methods, Object implementationSource, Map<String, Invocable> map
        ) {
            for (Method method : methods) {
                final int mod = method.getModifiers();

                Method implMethod;
                if ((
                        implMethod = findMatchingMethod(method, implementationSource.getClass())
                ) != null) {
                    map.put(methodString(method),
                            Invocable.ofMethodCall(implMethod, implementationSource)
                    );
                }
            }
        }

        static @Nullable Method findMatchingMethod(Method abstractMethod, Class<?> inClass) {
            for (Method method : inClass.getMethods()) {
                if (matchFootprint(abstractMethod, method)) {
                    return method;
                }
            }

            // TODO: 02.02.2020 doesnt work

            return null;
        }

        private static boolean matchFootprint(Method abstractMethod, Method method) {
            return abstractMethod.getName()
                    .equals(method.getName()) && Arrays.equals(
                    abstractMethod.getParameterTypes(),
                    method.getParameterTypes()
            ) && abstractMethod.getReturnType()
                    .equals(method.getReturnType());
        }

        public Builder<T> subImplement(Object sub, Class<?> asInterface) {
            final Class<?> subClass = sub.getClass();
            Stream.of(subClass.getMethods())
                    .filter(method -> method.getDeclaringClass()
                            .equals(subClass))
                    .forEach(method -> Stream.of(asInterface.getMethods())
                            .filter(other -> matchFootprint(other, method))
                            .findAny()
                            .ifPresent(value -> methodBinds.put(methodString(value),
                                    Invocable.ofMethodCall(method, sub)
                            )));

            interfaces.add(asInterface);

            return this;
        }

        public Builder<T> classloader(ClassLoader classLoader) {
            this.classLoader = classLoader;

            return this;
        }
    }

    private Spellbind() {
        throw new UnsupportedOperationException("Cannot instantiate " + Spellbind.class.getName());
    }
}