package org.comroid.spellbind;

import org.comroid.common.func.Invocable;
import org.comroid.common.map.TrieMap;
import org.comroid.spellbind.annotation.Partial;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Stream;

import static org.comroid.spellbind.SpellCore.methodString;

public final class Spellbind {
    private Spellbind() {
        throw new UnsupportedOperationException("Cannot instantiate " + Spellbind.class.getName());
    }

    public static <T> Builder<T> builder(Class<T> mainInterface) {
        return new Builder<>(mainInterface);
    }

    public static class Builder<T> implements org.comroid.common.func.Builder<T> {
        private final Class<T> mainInterface;
        private final Map<String, Invocable<Object>> methodBinds;
        private final Collection<Class<?>> interfaces;
        private Object coreObject;
        private ClassLoader classLoader;

        public Builder(Class<T> mainInterface) {
            this.mainInterface = mainInterface;
            this.methodBinds = TrieMap.ofString();
            this.interfaces = new ArrayList<>(1);

            interfaces.add(mainInterface);
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
                    .equals(method.getName()) && Arrays.equals(abstractMethod.getParameterTypes(), method.getParameterTypes()) &&
                    abstractMethod.getReturnType()
                            .equals(method.getReturnType());
        }

        @Override
        public T build() {
            final ClassLoader classLoader = Optional.ofNullable(this.classLoader)
                    .orElseGet(Spellbind.class::getClassLoader);

            final SpellCore spellCore = new SpellCore(coreObject, methodBinds);

            //noinspection unchecked
            return (T) Proxy.newProxyInstance(
                    classLoader,
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
                Method[] methods, Object implementationSource, Map<String, Invocable<Object>> map
        ) {
            for (Method method : methods) {
                final int mod = method.getModifiers();

                Method implMethod;
                if ((
                        implMethod = findMatchingMethod(method, implementationSource.getClass())
                ) != null) {
                    map.put(methodString(method), Invocable.ofMethodCall(implMethod, implementationSource));
                }
            }
        }

        public Builder<T> subImplement(Object sub) {
            return Partial.Support.findPartialClass(sub.getClass())
                    .map(partialInterface -> subImplement(sub, partialInterface))
                    .orElseThrow(() -> new IllegalArgumentException(String.format(
                            "Class %s implements no @Partial annotated class",
                            sub.getClass()
                                    .getName()
                    )));
        }

        public Builder<T> subImplement(Object sub, Class<?> asInterface) {
            if (!Modifier.isInterface(asInterface.getModifiers())) {
                throw new IllegalArgumentException(String.format("Class %s is not an interface!", asInterface.getName()));
            }

            final Class<?> subClass = sub.getClass();
            Stream.of(subClass.getMethods())
                    .filter(method -> method.getDeclaringClass()
                            .equals(subClass))
                    .forEach(method -> Stream.of(asInterface.getMethods())
                            .filter(other -> matchFootprint(other, method))
                            .findAny()
                            .ifPresent(value -> methodBinds.put(methodString(value), Invocable.ofMethodCall(method, sub))));

            interfaces.add(asInterface);

            return this;
        }

        public Builder<T> classloader(ClassLoader classLoader) {
            this.classLoader = classLoader;

            return this;
        }
    }
}